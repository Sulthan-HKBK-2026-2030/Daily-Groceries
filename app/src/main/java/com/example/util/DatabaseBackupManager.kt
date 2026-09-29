package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.data.local.dao.CashbookDao
import com.example.data.local.dao.GroceryDao
import com.example.data.local.dao.SodaDao
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.CustomCategoryEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.GroceryItemEntity
import com.example.data.local.entity.SodaSaleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupStats(
    val groceryCount: Int,
    val transactionCount: Int,
    val sodaSaleCount: Int,
    val categoryCount: Int
)

object DatabaseBackupManager {

    suspend fun exportDatabaseToJson(
        groceryDao: GroceryDao,
        cashbookDao: CashbookDao,
        sodaDao: SodaDao
    ): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("app", "DailyGrocer")
        root.put("schemaVersion", 1)
        root.put("exportTimestamp", System.currentTimeMillis())
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        root.put("exportDateFormatted", dateStr)

        // 1. Budget Settings
        val budget = cashbookDao.getBudgetSettingDirect() ?: BudgetSettingEntity()
        val budgetObj = JSONObject().apply {
            put("id", budget.id)
            put("dailyBudgetLimit", budget.dailyBudgetLimit)
            put("weeklyBudgetLimit", budget.weeklyBudgetLimit)
            put("updatedTimestamp", budget.updatedTimestamp)
        }
        root.put("budgetSettings", budgetObj)

        // 2. Groceries
        val groceries = groceryDao.getAllItems().firstOrNull() ?: emptyList()
        val groceryArray = JSONArray()
        groceries.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("name", item.name)
                put("nativeName", item.nativeName)
                put("category", item.category)
                put("unit", item.unit)
                put("pricePerUnit", item.pricePerUnit)
                put("iconKey", item.iconKey)
                put("isFavorite", item.isFavorite)
                put("inMonthlyChecklist", item.inMonthlyChecklist)
                put("monthlyQtySingle", item.monthlyQtySingle)
                put("monthlyQtySmall", item.monthlyQtySmall)
                put("monthlyQtyFamily", item.monthlyQtyFamily)
                put("isCheckedInMonthlyList", item.isCheckedInMonthlyList)
            }
            groceryArray.put(obj)
        }
        root.put("groceries", groceryArray)

        // 3. Transactions
        val transactions = cashbookDao.getAllTransactions().firstOrNull() ?: emptyList()
        val transArray = JSONArray()
        transactions.forEach { t ->
            val obj = JSONObject().apply {
                put("id", t.id)
                put("type", t.type)
                put("category", t.category)
                put("title", t.title)
                put("amount", t.amount)
                put("dateMillis", t.dateMillis)
                put("note", t.note)
            }
            transArray.put(obj)
        }
        root.put("transactions", transArray)

        // 4. Soda Sales
        val sales = sodaDao.getAllSales().firstOrNull() ?: emptyList()
        val sodaArray = JSONArray()
        sales.forEach { s ->
            val obj = JSONObject().apply {
                put("id", s.id)
                put("flavor", s.flavor)
                put("bottlesSold", s.bottlesSold)
                put("pricePerBottle", s.pricePerBottle)
                put("totalRevenue", s.totalRevenue)
                put("dateMillis", s.dateMillis)
                put("notes", s.notes)
            }
            sodaArray.put(obj)
        }
        root.put("sodaSales", sodaArray)

        // 5. Custom Categories
        val customCats = cashbookDao.getAllCustomCategories().firstOrNull() ?: emptyList()
        val catArray = JSONArray()
        customCats.forEach { c ->
            val obj = JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("iconKey", c.iconKey)
                put("isIncome", c.isIncome)
            }
            catArray.put(obj)
        }
        root.put("customCategories", catArray)

        root.toString(2) // Pretty formatted with 2 space indent
    }

    suspend fun restoreDatabaseFromJson(
        jsonString: String,
        groceryDao: GroceryDao,
        cashbookDao: CashbookDao,
        sodaDao: SodaDao
    ): Result<BackupStats> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)

            // 1. Budget Settings
            if (root.has("budgetSettings")) {
                val bObj = root.getJSONObject("budgetSettings")
                val daily = bObj.optDouble("dailyBudgetLimit", 800.0)
                val weekly = bObj.optDouble("weeklyBudgetLimit", 5000.0)
                cashbookDao.setBudgetSetting(
                    BudgetSettingEntity(
                        id = 1,
                        dailyBudgetLimit = daily,
                        weeklyBudgetLimit = weekly,
                        updatedTimestamp = System.currentTimeMillis()
                    )
                )
            }

            // 2. Groceries
            var gCount = 0
            if (root.has("groceries")) {
                val gArray = root.getJSONArray("groceries")
                val list = mutableListOf<GroceryItemEntity>()
                for (i in 0 until gArray.length()) {
                    val obj = gArray.getJSONObject(i)
                    list.add(
                        GroceryItemEntity(
                            id = obj.optInt("id", 0),
                            name = obj.getString("name"),
                            nativeName = obj.optString("nativeName", ""),
                            category = obj.optString("category", "Groceries"),
                            unit = obj.optString("unit", "kg"),
                            pricePerUnit = obj.optDouble("pricePerUnit", 0.0),
                            iconKey = obj.optString("iconKey", "grocery"),
                            isFavorite = obj.optBoolean("isFavorite", false),
                            inMonthlyChecklist = obj.optBoolean("inMonthlyChecklist", true),
                            monthlyQtySingle = obj.optString("monthlyQtySingle", "1 kg"),
                            monthlyQtySmall = obj.optString("monthlyQtySmall", "2.5 kg"),
                            monthlyQtyFamily = obj.optString("monthlyQtyFamily", "5 kg"),
                            isCheckedInMonthlyList = obj.optBoolean("isCheckedInMonthlyList", false)
                        )
                    )
                }
                if (list.isNotEmpty()) {
                    groceryDao.insertAll(list)
                    gCount = list.size
                }
            }

            // 3. Transactions
            var tCount = 0
            if (root.has("transactions")) {
                val tArray = root.getJSONArray("transactions")
                for (i in 0 until tArray.length()) {
                    val obj = tArray.getJSONObject(i)
                    cashbookDao.insertTransaction(
                        ExpenseIncomeEntity(
                            id = obj.optLong("id", 0L),
                            type = obj.optString("type", "EXPENSE"),
                            category = obj.optString("category", "Groceries"),
                            title = obj.optString("title", ""),
                            amount = obj.optDouble("amount", 0.0),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                    tCount++
                }
            }

            // 4. Soda Sales
            var sCount = 0
            if (root.has("sodaSales")) {
                val sArray = root.getJSONArray("sodaSales")
                for (i in 0 until sArray.length()) {
                    val obj = sArray.getJSONObject(i)
                    sodaDao.insertSale(
                        SodaSaleEntity(
                            id = obj.optLong("id", 0L),
                            flavor = obj.optString("flavor", "Paneer Soda"),
                            bottlesSold = obj.optInt("bottlesSold", 1),
                            pricePerBottle = obj.optDouble("pricePerBottle", 7.0),
                            totalRevenue = obj.optDouble("totalRevenue", 7.0),
                            dateMillis = obj.optLong("dateMillis", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                    sCount++
                }
            }

            // 5. Custom Categories
            var cCount = 0
            if (root.has("customCategories")) {
                val cArray = root.getJSONArray("customCategories")
                for (i in 0 until cArray.length()) {
                    val obj = cArray.getJSONObject(i)
                    cashbookDao.insertCustomCategory(
                        CustomCategoryEntity(
                            id = obj.optLong("id", 0L),
                            name = obj.getString("name"),
                            iconKey = obj.optString("iconKey", obj.optString("icon", "category")),
                            isIncome = obj.optBoolean("isIncome", false)
                        )
                    )
                    cCount++
                }
            }

            Result.success(BackupStats(gCount, tCount, sCount, cCount))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareJsonDump(context: Context, jsonString: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "DailyGrocer_Database_Backup_${System.currentTimeMillis()}.json")
            putExtra(Intent.EXTRA_TEXT, jsonString)
            type = "application/json"
        }
        val chooser = Intent.createChooser(sendIntent, "Save or Share Database Backup JSON")
        context.startActivity(chooser)
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DailyGrocer_Backup_JSON", text)
        clipboard.setPrimaryClip(clip)
    }
}
