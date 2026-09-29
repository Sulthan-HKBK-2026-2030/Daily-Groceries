package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_settings")
data class BudgetSettingEntity(
    @PrimaryKey
    val id: Int = 1,
    val dailyBudgetLimit: Double = 800.0,
    val weeklyBudgetLimit: Double = 5000.0,
    val updatedTimestamp: Long = System.currentTimeMillis()
)
