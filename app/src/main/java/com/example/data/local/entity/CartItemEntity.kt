package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groceryItemId: Int,
    val name: String,
    val nativeName: String,
    val category: String,
    val iconKey: String,
    val unitPrice: Double,
    val unit: String,
    val selectedQuantityValue: Double, // e.g. 250 (grams) or 1.5 (kg) or 2 (packs)
    val quantityLabel: String, // e.g. "250 Grams", "1 Kg", "500 Grams", "750 Grams"
    val calculatedPrice: Double,
    val timestamp: Long = System.currentTimeMillis()
)
