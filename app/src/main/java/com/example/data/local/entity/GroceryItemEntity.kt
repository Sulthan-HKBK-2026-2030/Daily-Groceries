package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grocery_items")
data class GroceryItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val nativeName: String,
    val category: String,
    val iconKey: String,
    val pricePerUnit: Double, // Daily manual price (e.g. ₹40/kg)
    val unit: String = "kg", // "kg", "L", "pack", "pc", "bunch"
    val isFavorite: Boolean = false,
    val inMonthlyChecklist: Boolean = true,
    val monthlyQtySingle: String = "1 kg",
    val monthlyQtySmall: String = "2.5 kg",
    val monthlyQtyFamily: String = "5 kg",
    val isCheckedInMonthlyList: Boolean = false
)
