package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses_incomes")
data class ExpenseIncomeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "EXPENSE" or "INCOME"
    val category: String, // 'Household Bills', 'Transportation', 'Entertainment', 'Personal Care', 'Miscellaneous', 'Groceries', 'Goli Soda Business', or custom
    val title: String,
    val amount: Double,
    val dateMillis: Long = System.currentTimeMillis(),
    val note: String = ""
)
