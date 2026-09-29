package com.example.data.repository

import com.example.data.local.dao.CashbookDao
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.CustomCategoryEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import kotlinx.coroutines.flow.Flow

class CashbookRepository(private val cashbookDao: CashbookDao) {

    val allTransactions: Flow<List<ExpenseIncomeEntity>> =
        cashbookDao.getAllTransactions()

    val customCategories: Flow<List<CustomCategoryEntity>> =
        cashbookDao.getAllCustomCategories()

    val budgetSetting: Flow<BudgetSettingEntity?> =
        cashbookDao.getBudgetSetting()

    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<ExpenseIncomeEntity>> =
        cashbookDao.getTransactionsBetween(startTime, endTime)

    suspend fun insertTransaction(transaction: ExpenseIncomeEntity) =
        cashbookDao.insertTransaction(transaction)

    suspend fun deleteTransaction(id: Long) =
        cashbookDao.deleteTransaction(id)

    suspend fun addCustomCategory(category: CustomCategoryEntity) =
        cashbookDao.insertCustomCategory(category)

    suspend fun deleteCustomCategory(id: Long) =
        cashbookDao.deleteCustomCategory(id)

    suspend fun updateDailyBudget(limit: Double) {
        val current = cashbookDao.getBudgetSettingDirect() ?: BudgetSettingEntity()
        cashbookDao.setBudgetSetting(current.copy(dailyBudgetLimit = limit, updatedTimestamp = System.currentTimeMillis()))
    }

    suspend fun updateWeeklyBudget(limit: Double) {
        val current = cashbookDao.getBudgetSettingDirect() ?: BudgetSettingEntity()
        cashbookDao.setBudgetSetting(current.copy(weeklyBudgetLimit = limit, updatedTimestamp = System.currentTimeMillis()))
    }
}
