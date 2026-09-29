package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    @Query("SELECT * FROM grocery_items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE name LIKE '%' || :query || '%' OR nativeName LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchItems(query: String): Flow<List<GroceryItemEntity>>

    @Query("SELECT * FROM grocery_items WHERE id = :id")
    fun getItemById(id: Int): Flow<GroceryItemEntity?>

    @Query("SELECT COUNT(*) FROM grocery_items")
    suspend fun getItemCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<GroceryItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: GroceryItemEntity): Long

    @Update
    suspend fun update(item: GroceryItemEntity)

    @Query("UPDATE grocery_items SET pricePerUnit = :newPrice WHERE id = :id")
    suspend fun updatePrice(id: Int, newPrice: Double)

    @Query("UPDATE grocery_items SET isCheckedInMonthlyList = :isChecked WHERE id = :id")
    suspend fun toggleMonthlyCheck(id: Int, isChecked: Boolean)

    @Query("UPDATE grocery_items SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Int, isFav: Boolean)

    @Query("DELETE FROM grocery_items WHERE id = :id")
    suspend fun deleteById(id: Int)

    // Cart Queries
    @Query("SELECT * FROM cart_items ORDER BY timestamp DESC")
    fun getCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItemEntity): Long

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Long)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}
