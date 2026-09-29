package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.initial.DefaultGroceryData
import com.example.data.local.dao.CashbookDao
import com.example.data.local.dao.GroceryDao
import com.example.data.local.dao.SodaDao
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.CustomCategoryEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.GroceryItemEntity
import com.example.data.local.entity.SodaSaleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GroceryItemEntity::class,
        CartItemEntity::class,
        ExpenseIncomeEntity::class,
        SodaSaleEntity::class,
        CustomCategoryEntity::class,
        BudgetSettingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun groceryDao(): GroceryDao
    abstract fun cashbookDao(): CashbookDao
    abstract fun sodaDao(): SodaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "daily_grocer_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialDatabase(database)
                    }
                }
            }

            suspend fun populateInitialDatabase(database: AppDatabase) {
                val groceryDao = database.groceryDao()
                if (groceryDao.getItemCount() == 0) {
                    groceryDao.insertAll(DefaultGroceryData.initialItems)
                }

                val cashbookDao = database.cashbookDao()
                // Initial Default Expense Categories requested by user
                val defaultCategories = listOf(
                    CustomCategoryEntity(name = "Groceries", iconKey = "groceries", isIncome = false),
                    CustomCategoryEntity(name = "Utilities", iconKey = "utilities", isIncome = false),
                    CustomCategoryEntity(name = "Wages", iconKey = "wages", isIncome = false),
                    CustomCategoryEntity(name = "Household Bills", iconKey = "bills", isIncome = false),
                    CustomCategoryEntity(name = "Transportation", iconKey = "transport", isIncome = false),
                    CustomCategoryEntity(name = "Entertainment", iconKey = "entertainment", isIncome = false),
                    CustomCategoryEntity(name = "Personal Care", iconKey = "personal_care", isIncome = false),
                    CustomCategoryEntity(name = "Miscellaneous", iconKey = "misc", isIncome = false),
                    CustomCategoryEntity(name = "Goli Soda Business", iconKey = "soda", isIncome = false),
                    CustomCategoryEntity(name = "Goli Soda Sales", iconKey = "soda_income", isIncome = true),
                    CustomCategoryEntity(name = "Daily Wages / Salary", iconKey = "wages", isIncome = true)
                )
                for (cat in defaultCategories) {
                    cashbookDao.insertCustomCategory(cat)
                }

                // Initial Daily Budget default (e.g. ₹800/day)
                cashbookDao.setBudgetSetting(BudgetSettingEntity(id = 1, dailyBudgetLimit = 800.0))

                // Initial Seed Expenses across Groceries, Utilities, Wages, and Soda
                if (cashbookDao.getTransactionCount() == 0) {
                    val now = System.currentTimeMillis()
                    val sampleExpenses = listOf(
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Groceries",
                            title = "Country Tomatoes & Onions",
                            amount = 85.0,
                            dateMillis = now - 3600000 * 2,
                            note = "Fresh vegetables from local mandi"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Groceries",
                            title = "Aavin Milk & Curd",
                            amount = 55.0,
                            dateMillis = now - 3600000 * 5,
                            note = "Daily morning dairy"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Utilities",
                            title = "TNEB Electricity Bill",
                            amount = 450.0,
                            dateMillis = now - 3600000 * 24,
                            note = "Shop & kitchen power bill"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Utilities",
                            title = "Cooking Gas Cylinder Refill",
                            amount = 850.0,
                            dateMillis = now - 3600000 * 48,
                            note = "Indane commercial refill"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Wages",
                            title = "Shop Assistant Daily Wage",
                            amount = 350.0,
                            dateMillis = now - 3600000 * 4,
                            note = "Soda counter evening helper"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Wages",
                            title = "Soda Bottle Cleaner Wage",
                            amount = 250.0,
                            dateMillis = now - 3600000 * 28,
                            note = "Bottle washing and crate sorting"
                        ),
                        ExpenseIncomeEntity(
                            type = "EXPENSE",
                            category = "Goli Soda Business",
                            title = "Ice Block & Syrups",
                            amount = 120.0,
                            dateMillis = now - 3600000 * 8,
                            note = "Lemon & panneer soda stock"
                        )
                    )
                    for (exp in sampleExpenses) {
                        cashbookDao.insertTransaction(exp)
                    }
                }
            }
        }
    }
}
