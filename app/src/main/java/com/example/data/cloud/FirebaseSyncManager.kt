package com.example.data.cloud

import android.content.Context
import android.util.Log
import com.example.data.local.dao.CashbookDao
import com.example.data.local.dao.GroceryDao
import com.example.data.local.dao.SodaDao
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.GroceryItemEntity
import com.example.data.local.entity.SodaSaleEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

sealed class SyncStatus {
    object Idle : SyncStatus()
    object Syncing : SyncStatus()
    data class Success(val message: String, val timestamp: Long = System.currentTimeMillis()) : SyncStatus()
    data class Error(val errorMessage: String) : SyncStatus()
    object NotConfigured : SyncStatus()
}

class FirebaseSyncManager private constructor() {

    companion object {
        private const val TAG = "FirebaseSyncManager"

        @Volatile
        private var INSTANCE: FirebaseSyncManager? = null

        fun getInstance(): FirebaseSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseSyncManager().also { INSTANCE = it }
            }
        }
    }

    private var auth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _isFirebaseAvailable = MutableStateFlow(false)
    val isFirebaseAvailable: StateFlow<Boolean> = _isFirebaseAvailable.asStateFlow()

    fun initialize(context: Context) {
        try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            auth = FirebaseAuth.getInstance()
            firestore = FirebaseFirestore.getInstance()
            _isFirebaseAvailable.value = true
            _currentUser.value = auth?.currentUser

            auth?.addAuthStateListener { firebaseAuth ->
                _currentUser.value = firebaseAuth.currentUser
            }
            Log.d(TAG, "Firebase initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Firebase not yet configured or google-services.json missing: ${e.message}")
            _isFirebaseAvailable.value = false
            _syncStatus.value = SyncStatus.NotConfigured
        }
    }

    fun signInWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onError("Firebase is not initialized. Please ensure google-services.json is configured.")
            return
        }
        authInstance.signInWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { result ->
                result.user?.let {
                    _currentUser.value = it
                    onSuccess(it)
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to sign in")
            }
    }

    fun signUpWithEmail(
        email: String,
        pass: String,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onError("Firebase is not initialized. Please ensure google-services.json is configured.")
            return
        }
        authInstance.createUserWithEmailAndPassword(email.trim(), pass)
            .addOnSuccessListener { result ->
                result.user?.let {
                    _currentUser.value = it
                    onSuccess(it)
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to create account")
            }
    }

    fun signInAnonymously(
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        val authInstance = auth
        if (authInstance == null) {
            onError("Firebase is not configured.")
            return
        }
        authInstance.signInAnonymously()
            .addOnSuccessListener { result ->
                result.user?.let {
                    _currentUser.value = it
                    onSuccess(it)
                }
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Anonymous sign-in failed")
            }
    }

    fun signOut() {
        auth?.signOut()
        _currentUser.value = null
        _syncStatus.value = SyncStatus.Idle
    }

    suspend fun backupToCloud(
        groceryDao: GroceryDao,
        cashbookDao: CashbookDao,
        sodaDao: SodaDao
    ): Result<String> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
        val db = firestore
        if (user == null || db == null) {
            val error = "Must be signed in to sync data to Cloud."
            _syncStatus.value = SyncStatus.Error(error)
            return@withContext Result.failure(Exception(error))
        }

        _syncStatus.value = SyncStatus.Syncing

        try {
            val uid = user.uid
            val userDoc = db.collection("users").document(uid)

            // 1. Sync Budget Setting
            val budget = cashbookDao.getBudgetSettingDirect() ?: BudgetSettingEntity()
            userDoc.collection("settings").document("budget")
                .set(
                    mapOf(
                        "dailyBudgetLimit" to budget.dailyBudgetLimit,
                        "weeklyBudgetLimit" to budget.weeklyBudgetLimit,
                        "updatedTimestamp" to budget.updatedTimestamp
                    ),
                    SetOptions.merge()
                ).await()

            // 2. Sync Grocery items
            val allGroceries = groceryDao.getAllItems().firstOrNull() ?: emptyList()
            val groceryBatch = db.batch()
            allGroceries.forEach { item ->
                val doc = userDoc.collection("groceries").document(item.id.toString())
                groceryBatch.set(
                    doc,
                    mapOf(
                        "id" to item.id,
                        "name" to item.name,
                        "nativeName" to item.nativeName,
                        "category" to item.category,
                        "unit" to item.unit,
                        "pricePerUnit" to item.pricePerUnit,
                        "iconKey" to item.iconKey,
                        "isFavorite" to item.isFavorite,
                        "inMonthlyChecklist" to item.inMonthlyChecklist,
                        "monthlyQtySingle" to item.monthlyQtySingle,
                        "monthlyQtySmall" to item.monthlyQtySmall,
                        "monthlyQtyFamily" to item.monthlyQtyFamily,
                        "isCheckedInMonthlyList" to item.isCheckedInMonthlyList
                    ),
                    SetOptions.merge()
                )
            }
            groceryBatch.commit().await()

            // 3. Sync Transactions (Cashbook)
            val allTransactions = cashbookDao.getAllTransactions().firstOrNull() ?: emptyList()
            val transBatch = db.batch()
            allTransactions.forEach { trans ->
                val doc = userDoc.collection("transactions").document(trans.id.toString())
                transBatch.set(
                    doc,
                    mapOf(
                        "id" to trans.id,
                        "type" to trans.type,
                        "category" to trans.category,
                        "title" to trans.title,
                        "amount" to trans.amount,
                        "dateMillis" to trans.dateMillis,
                        "note" to trans.note
                    ),
                    SetOptions.merge()
                )
            }
            transBatch.commit().await()

            // 4. Sync Goli Soda Sales
            val allSodaSales = sodaDao.getAllSales().firstOrNull() ?: emptyList()
            val sodaBatch = db.batch()
            allSodaSales.forEach { sale ->
                val doc = userDoc.collection("soda_sales").document(sale.id.toString())
                sodaBatch.set(
                    doc,
                    mapOf(
                        "id" to sale.id,
                        "flavor" to sale.flavor,
                        "bottlesSold" to sale.bottlesSold,
                        "pricePerBottle" to sale.pricePerBottle,
                        "totalRevenue" to sale.totalRevenue,
                        "dateMillis" to sale.dateMillis,
                        "notes" to sale.notes
                    ),
                    SetOptions.merge()
                )
            }
            sodaBatch.commit().await()

            val successMsg = "Successfully backed up ${allGroceries.size} grocery items, ${allTransactions.size} transactions, and ${allSodaSales.size} soda logs to Firestore!"
            _syncStatus.value = SyncStatus.Success(successMsg)
            Result.success(successMsg)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Sync error occurred"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(
        groceryDao: GroceryDao,
        cashbookDao: CashbookDao,
        sodaDao: SodaDao
    ): Result<String> = withContext(Dispatchers.IO) {
        val user = auth?.currentUser
        val db = firestore
        if (user == null || db == null) {
            val error = "Must be signed in to restore data from Cloud."
            _syncStatus.value = SyncStatus.Error(error)
            return@withContext Result.failure(Exception(error))
        }

        _syncStatus.value = SyncStatus.Syncing

        try {
            val uid = user.uid
            val userDoc = db.collection("users").document(uid)

            // 1. Restore Budget Settings
            val budgetDoc = userDoc.collection("settings").document("budget").get().await()
            if (budgetDoc.exists()) {
                val daily = budgetDoc.getDouble("dailyBudgetLimit") ?: 800.0
                val weekly = budgetDoc.getDouble("weeklyBudgetLimit") ?: 5000.0
                cashbookDao.setBudgetSetting(
                    BudgetSettingEntity(
                        id = 1,
                        dailyBudgetLimit = daily,
                        weeklyBudgetLimit = weekly,
                        updatedTimestamp = System.currentTimeMillis()
                    )
                )
            }

            // 2. Restore Groceries
            val groceriesSnap = userDoc.collection("groceries").get().await()
            var groceryCount = 0
            val restoredGroceries = groceriesSnap.documents.mapNotNull { doc ->
                try {
                    GroceryItemEntity(
                        id = doc.getLong("id")?.toInt() ?: 0,
                        name = doc.getString("name") ?: "",
                        nativeName = doc.getString("nativeName") ?: "",
                        category = doc.getString("category") ?: "Groceries",
                        unit = doc.getString("unit") ?: "kg",
                        pricePerUnit = doc.getDouble("pricePerUnit") ?: 0.0,
                        iconKey = doc.getString("iconKey") ?: "grocery",
                        isFavorite = doc.getBoolean("isFavorite") ?: false,
                        inMonthlyChecklist = doc.getBoolean("inMonthlyChecklist") ?: true,
                        monthlyQtySingle = doc.getString("monthlyQtySingle") ?: "1 kg",
                        monthlyQtySmall = doc.getString("monthlyQtySmall") ?: "2.5 kg",
                        monthlyQtyFamily = doc.getString("monthlyQtyFamily") ?: "5 kg",
                        isCheckedInMonthlyList = doc.getBoolean("isCheckedInMonthlyList") ?: false
                    )
                } catch (_: Exception) {
                    null
                }
            }
            if (restoredGroceries.isNotEmpty()) {
                groceryDao.insertAll(restoredGroceries)
                groceryCount = restoredGroceries.size
            }

            // 3. Restore Transactions
            val transSnap = userDoc.collection("transactions").get().await()
            var transCount = 0
            transSnap.documents.forEach { doc ->
                try {
                    val id = doc.getLong("id") ?: 0L
                    val item = ExpenseIncomeEntity(
                        id = id,
                        type = doc.getString("type") ?: "EXPENSE",
                        category = doc.getString("category") ?: "Groceries",
                        title = doc.getString("title") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        note = doc.getString("note") ?: ""
                    )
                    cashbookDao.insertTransaction(item)
                    transCount++
                } catch (_: Exception) {}
            }

            // 4. Restore Soda Sales
            val sodaSnap = userDoc.collection("soda_sales").get().await()
            var sodaCount = 0
            sodaSnap.documents.forEach { doc ->
                try {
                    val id = doc.getLong("id") ?: 0L
                    val sale = SodaSaleEntity(
                        id = id,
                        flavor = doc.getString("flavor") ?: "Paneer Soda",
                        bottlesSold = doc.getLong("bottlesSold")?.toInt() ?: 1,
                        pricePerBottle = doc.getDouble("pricePerBottle") ?: 7.0,
                        totalRevenue = doc.getDouble("totalRevenue") ?: 7.0,
                        dateMillis = doc.getLong("dateMillis") ?: System.currentTimeMillis(),
                        notes = doc.getString("notes") ?: ""
                    )
                    sodaDao.insertSale(sale)
                    sodaCount++
                } catch (_: Exception) {}
            }

            val successMsg = "Restored $groceryCount grocery items, $transCount transactions, and $sodaCount soda sales from Firestore!"
            _syncStatus.value = SyncStatus.Success(successMsg)
            Result.success(successMsg)
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Restore error occurred"
            _syncStatus.value = SyncStatus.Error(errorMsg)
            Result.failure(e)
        }
    }
}
