package com.example.data.repository

import com.example.data.local.dao.GroceryDao
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.GroceryItemEntity
import kotlinx.coroutines.flow.Flow

class GroceryRepository(private val groceryDao: GroceryDao) {

    val allItems: Flow<List<GroceryItemEntity>> = groceryDao.getAllItems()
    val cartItems: Flow<List<CartItemEntity>> = groceryDao.getCartItems()

    fun getItemsByCategory(category: String): Flow<List<GroceryItemEntity>> =
        groceryDao.getItemsByCategory(category)

    fun searchItems(query: String): Flow<List<GroceryItemEntity>> =
        groceryDao.searchItems(query)

    suspend fun updatePrice(id: Int, newPrice: Double) =
        groceryDao.updatePrice(id, newPrice)

    suspend fun toggleFavorite(id: Int, isFav: Boolean) =
        groceryDao.toggleFavorite(id, isFav)

    suspend fun toggleMonthlyCheck(id: Int, isChecked: Boolean) =
        groceryDao.toggleMonthlyCheck(id, isChecked)

    suspend fun insertItem(item: GroceryItemEntity) =
        groceryDao.insert(item)

    suspend fun deleteItem(id: Int) =
        groceryDao.deleteById(id)

    suspend fun addToCart(item: CartItemEntity) =
        groceryDao.insertCartItem(item)

    suspend fun removeCartItem(id: Long) =
        groceryDao.deleteCartItem(id)

    suspend fun clearCart() =
        groceryDao.clearCart()

    suspend fun ensureDefaultDataLoaded() {
        if (groceryDao.getItemCount() == 0) {
            groceryDao.insertAll(com.example.data.initial.DefaultGroceryData.initialItems)
        }
    }
}
