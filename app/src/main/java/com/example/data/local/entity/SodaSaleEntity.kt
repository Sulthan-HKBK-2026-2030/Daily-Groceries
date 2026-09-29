package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "soda_sales")
data class SodaSaleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val flavor: String, // "Paneer Soda", "Lemon Soda", "Orange Soda", "Cola", "Masala Soda", etc.
    val bottlesSold: Int,
    val pricePerBottle: Double = 7.0, // default ₹7
    val totalRevenue: Double = bottlesSold * pricePerBottle,
    val dateMillis: Long = System.currentTimeMillis(),
    val notes: String = ""
)
