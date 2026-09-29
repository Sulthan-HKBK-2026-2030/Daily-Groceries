package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.GroceryItemEntity
import com.example.data.repository.GroceryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository

    val allCategories = listOf(
        "All Items",
        "Vegetables",
        "Fruits",
        "Spices & Condiments",
        "Lentils & Pulses",
        "Flours & Grains",
        "Oils & Ghee",
        "Staples & Condiments",
        "Beverages",
        "Snacks & Sweets",
        "Dairy Products",
        "Household & Care"
    )

    private val _selectedCategory = MutableStateFlow("All Items")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _calculatorSelectedItem = MutableStateFlow<GroceryItemEntity?>(null)
    val calculatorSelectedItem: StateFlow<GroceryItemEntity?> = _calculatorSelectedItem.asStateFlow()

    private val _isCartOpen = MutableStateFlow(false)
    val isCartOpen: StateFlow<Boolean> = _isCartOpen.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = GroceryRepository(database.groceryDao())
        viewModelScope.launch(Dispatchers.IO) {
            repository.ensureDefaultDataLoaded()
        }
    }

    val cartItems: StateFlow<List<CartItemEntity>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredItems: StateFlow<List<GroceryItemEntity>> = combine(
        repository.allItems,
        _selectedCategory,
        _searchQuery
    ) { items, category, query ->
        items.filter { item ->
            val matchesCategory = category == "All Items" || item.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.nativeName.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthlyChecklistItems: StateFlow<List<GroceryItemEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openCalculator(item: GroceryItemEntity) {
        _calculatorSelectedItem.value = item
    }

    fun closeCalculator() {
        _calculatorSelectedItem.value = null
    }

    fun openCart() {
        _isCartOpen.value = true
    }

    fun closeCart() {
        _isCartOpen.value = false
    }

    fun updateItemPrice(id: Int, newPrice: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePrice(id, newPrice)
        }
    }

    fun toggleMonthlyCheck(id: Int, isChecked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleMonthlyCheck(id, isChecked)
        }
    }

    fun toggleFavorite(id: Int, isFav: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFavorite(id, isFav)
        }
    }

    fun addToCart(cartItem: CartItemEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addToCart(cartItem)
        }
    }

    fun removeCartItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.removeCartItem(id)
        }
    }

    fun clearCart() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCart()
        }
    }

    fun addNewItem(
        name: String,
        nativeName: String,
        category: String,
        pricePerUnit: Double,
        unit: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = GroceryItemEntity(
                name = name,
                nativeName = nativeName,
                category = category,
                iconKey = name.lowercase().replace(" ", "_"),
                pricePerUnit = pricePerUnit,
                unit = unit
            )
            repository.insertItem(item)
        }
    }
}
