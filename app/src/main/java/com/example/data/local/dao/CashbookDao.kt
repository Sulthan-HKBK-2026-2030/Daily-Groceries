package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.CustomCategoryEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashbookDao {
    @Query("SELECT * FROM expenses_incomes ORDER BY dateMillis DESC")
    fun getAllTransactions(): Flow<List<ExpenseIncomeEntity>>

    @Query("SELECT * FROM expenses_incomes WHERE dateMillis >= :startTime AND dateMillis <= :endTime ORDER BY dateMillis DESC")
    fun getTransactionsBetween(startTime: Long, endTime: Long): Flow<List<ExpenseIncomeEntity>>

    @Query("SELECT * FROM expenses_incomes WHERE type = :type ORDER BY dateMillis DESC")
    fun getTransactionsByType(type: String): Flow<List<ExpenseIncomeEntity>>

    @Query("SELECT COUNT(*) FROM expenses_incomes")
    suspend fun getTransactionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: ExpenseIncomeEntity): Long

    @Query("DELETE FROM expenses_incomes WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    // Custom Categories
    @Query("SELECT * FROM custom_categories ORDER BY name ASC")
    fun getAllCustomCategories(): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCategory(category: CustomCategoryEntity): Long

    @Query("DELETE FROM custom_categories WHERE id = :id")
    suspend fun deleteCustomCategory(id: Long)

    // Daily & Weekly Budget
    @Query("SELECT * FROM budget_settings WHERE id = 1")
    fun getBudgetSetting(): Flow<BudgetSettingEntity?>

    @Query("SELECT * FROM budget_settings WHERE id = 1")
    suspend fun getBudgetSettingDirect(): BudgetSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBudgetSetting(setting: BudgetSettingEntity)
}
