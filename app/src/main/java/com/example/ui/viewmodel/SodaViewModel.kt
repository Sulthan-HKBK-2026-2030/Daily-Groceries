package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.SodaSaleEntity
import com.example.data.repository.CashbookRepository
import com.example.data.repository.SodaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class FlavorCountState(
    val flavor: String,
    val count: Int,
    val icon: String
)

class SodaViewModel(application: Application) : AndroidViewModel(application) {

    private val sodaRepository: SodaRepository
    private val cashbookRepository: CashbookRepository

    // Price per soda bottle (default ₹7 as requested by Sulthan)
    private val _pricePerBottle = MutableStateFlow(7.0)
    val pricePerBottle: StateFlow<Double> = _pricePerBottle.asStateFlow()

    // Flavor counters
    private val defaultFlavors = listOf(
        FlavorCountState("Paneer Soda", 0, "🌹"),
        FlavorCountState("Lemon / Nannari", 0, "🍋"),
        FlavorCountState("Orange Soda", 0, "🍊"),
        FlavorCountState("Cola Goli Soda", 0, "🥤"),
        FlavorCountState("Ginger Masala", 0, "🫚"),
        FlavorCountState("Blueberry / Grape", 0, "🍇")
    )

    private val _flavorCounts = MutableStateFlow(defaultFlavors)
    val flavorCounts: StateFlow<List<FlavorCountState>> = _flavorCounts.asStateFlow()

    val allSales: StateFlow<List<SodaSaleEntity>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        sodaRepository = SodaRepository(database.sodaDao())
        cashbookRepository = CashbookRepository(database.cashbookDao())

        allSales = sodaRepository.allSales
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun setPricePerBottle(price: Double) {
        if (price > 0) {
            _pricePerBottle.value = price
        }
    }

    fun incrementFlavorCount(flavor: String, delta: Int) {
        _flavorCounts.value = _flavorCounts.value.map { item ->
            if (item.flavor == flavor) {
                val newCount = (item.count + delta).coerceAtLeast(0)
                item.copy(count = newCount)
            } else {
                item
            }
        }
    }

    fun setFlavorCount(flavor: String, count: Int) {
        _flavorCounts.value = _flavorCounts.value.map { item ->
            if (item.flavor == flavor) {
                item.copy(count = count.coerceAtLeast(0))
            } else {
                item
            }
        }
    }

    fun resetDailyCounter() {
        _flavorCounts.value = defaultFlavors
    }

    // Records the current active counter to database and into cashbook
    fun recordAndSyncSales(
        notes: String = "",
        onComplete: (totalBottles: Int, totalRevenue: Double) -> Unit
    ) {
        val activeSales = _flavorCounts.value.filter { it.count > 0 }
        if (activeSales.isEmpty()) return

        val price = _pricePerBottle.value
        val totalBottles = activeSales.sumOf { it.count }
        val totalRevenue = totalBottles * price

        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            for (flavorItem in activeSales) {
                val entity = SodaSaleEntity(
                    flavor = flavorItem.flavor,
                    bottlesSold = flavorItem.count,
                    pricePerBottle = price,
                    totalRevenue = flavorItem.count * price,
                    dateMillis = now,
                    notes = notes
                )
                sodaRepository.insertSale(entity)
            }

            // Sync with cashbook as INCOME under 'Goli Soda Business'
            val breakdown = activeSales.joinToString(", ") { "${it.flavor}: ${it.count} pcs" }
            val cashbookEntry = ExpenseIncomeEntity(
                type = "INCOME",
                category = "Goli Soda Business",
                title = "Goli Soda Sales ($totalBottles bottles sold)",
                amount = totalRevenue,
                dateMillis = now,
                note = "$breakdown | Rate: ₹$price/bottle. $notes".trim()
            )
            cashbookRepository.insertTransaction(cashbookEntry)

            // Reset counter
            _flavorCounts.value = defaultFlavors
        }

        onComplete(totalBottles, totalRevenue)
    }

    fun logSodaExpense(title: String, amount: Double, note: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val expense = ExpenseIncomeEntity(
                type = "EXPENSE",
                category = "Goli Soda Business",
                title = title,
                amount = amount,
                dateMillis = System.currentTimeMillis(),
                note = note
            )
            cashbookRepository.insertTransaction(expense)
        }
    }
}
