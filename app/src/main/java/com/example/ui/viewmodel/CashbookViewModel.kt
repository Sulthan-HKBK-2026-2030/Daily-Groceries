package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.BudgetSettingEntity
import com.example.data.local.entity.CustomCategoryEntity
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.repository.CashbookRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimeFilter {
    TODAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME
}

data class WeeklyDayTrend(
    val dayName: String,
    val dateDisplay: String,
    val startOfDayMillis: Long,
    val endOfDayMillis: Long,
    val expenseAmount: Double,
    val sodaIncomeAmount: Double,
    val totalIncomeAmount: Double,
    val isToday: Boolean
)

data class TodayDashboardSummary(
    val todayExpenses: Double,
    val todayGroceryExpenses: Double = 0.0,
    val todayOtherExpenses: Double = 0.0,
    val todaySodaIncome: Double,
    val todayOtherIncome: Double,
    val todayTotalIncome: Double,
    val dailyBudgetLimit: Double,
    val remainingBudget: Double,
    val budgetUsagePercent: Int,
    val netDailyBalance: Double,
    val todayTransactionsCount: Int,
    val todayExpenseItemsCount: Int = 0,
    val weeklyBudgetLimit: Double = 5000.0,
    val weeklyExpenses: Double = 0.0,
    val weeklyRemainingBudget: Double = 5000.0,
    val weeklyBudgetUsagePercent: Int = 0
)

data class FrequentPurchaseItem(
    val title: String,
    val nativeName: String,
    val category: String,
    val amount: Double,
    val iconKey: String,
    val frequency: Int = 1
)

data class WeekComparisonData(
    val currentWeekGrocery: Double,
    val previousWeekGrocery: Double,
    val groceryDiff: Double,
    val groceryPercentChange: Double,

    val currentWeekBusiness: Double,
    val previousWeekBusiness: Double,
    val businessDiff: Double,
    val businessPercentChange: Double,

    val currentWeekTotal: Double,
    val previousWeekTotal: Double,
    val totalDiff: Double,
    val totalPercentChange: Double,

    val savingsTips: List<String>,
    val currentWeekDateRange: String,
    val previousWeekDateRange: String
)

class CashbookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CashbookRepository

    val defaultExpenseCategories = listOf(
        "Groceries",
        "Utilities",
        "Wages",
        "Household Bills",
        "Goli Soda Business",
        "Transportation",
        "Entertainment",
        "Personal Care",
        "Miscellaneous"
    )

    val defaultIncomeCategories = listOf(
        "Goli Soda Business",
        "Daily Wages / Salary",
        "Wages",
        "Other Income"
    )

    private val _timeFilter = MutableStateFlow(TimeFilter.TODAY)
    val timeFilter: StateFlow<TimeFilter> = _timeFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow("ALL") // "ALL", "EXPENSE", "INCOME"
    val typeFilter: StateFlow<String> = _typeFilter.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow("ALL")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = CashbookRepository(database.cashbookDao())
    }

    val customCategories: StateFlow<List<CustomCategoryEntity>> = repository.customCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<ExpenseIncomeEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val budgetSetting: StateFlow<BudgetSettingEntity?> = repository.budgetSetting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BudgetSettingEntity(1, 800.0))

    val filteredTransactions: StateFlow<List<ExpenseIncomeEntity>> = combine(
        repository.allTransactions,
        _timeFilter,
        _typeFilter,
        _selectedCategoryFilter
    ) { transactions, time, type, cat ->
        val range = getTimeRange(time)
        transactions.filter { txn ->
            val matchesTime = range == null || (txn.dateMillis >= range.first && txn.dateMillis <= range.second)
            val matchesType = type == "ALL" || txn.type.equals(type, ignoreCase = true)
            val matchesCat = when {
                cat == "ALL" -> true
                cat.equals("Utilities", ignoreCase = true) ->
                    txn.category.contains("Utilit", ignoreCase = true) || txn.category.contains("Bill", ignoreCase = true)
                cat.equals("Wages", ignoreCase = true) ->
                    txn.category.contains("Wage", ignoreCase = true) || txn.category.contains("Salary", ignoreCase = true) || txn.category.contains("Labor", ignoreCase = true)
                cat.equals("Groceries", ignoreCase = true) ->
                    txn.category.contains("Grocer", ignoreCase = true) || txn.category.contains("Vegetable", ignoreCase = true) || txn.category.contains("Food", ignoreCase = true)
                else -> txn.category.equals(cat, ignoreCase = true)
            }
            matchesTime && matchesType && matchesCat
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chronological list of all expenses from Room database
    val allExpenses: StateFlow<List<ExpenseIncomeEntity>> = repository.allTransactions
        .combine(MutableStateFlow(Unit)) { transactions, _ ->
            transactions.filter {
                it.type.equals("EXPENSE", ignoreCase = true)
            }.sortedByDescending { it.dateMillis }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculations based on filtered time
    val currentSummary: StateFlow<Triple<Double, Double, Double>> = combine(
        repository.allTransactions,
        _timeFilter
    ) { transactions, time ->
        val range = getTimeRange(time)
        val validTxns = transactions.filter { txn ->
            range == null || (txn.dateMillis >= range.first && txn.dateMillis <= range.second)
        }
        val totalIncome = validTxns.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = validTxns.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netSavings = totalIncome - totalExpense
        Triple(totalIncome, totalExpense, netSavings)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Triple(0.0, 0.0, 0.0))

    // Today's summary for main dashboard with remaining daily budget
    val todayDashboardSummary: StateFlow<TodayDashboardSummary> = combine(
        repository.allTransactions,
        repository.budgetSetting
    ) { transactions, budget ->
        val todayRange = getTimeRange(TimeFilter.TODAY) ?: Pair(0L, Long.MAX_VALUE)
        val todayTxns = transactions.filter { it.dateMillis >= todayRange.first && it.dateMillis <= todayRange.second }

        val todayExpenseTxns = todayTxns.filter { it.type == "EXPENSE" }
        val todayExpenses = todayExpenseTxns.sumOf { it.amount }
        val todayGroceryExpenses = todayExpenseTxns.filter { it.category.contains("Grocer", ignoreCase = true) || it.category.contains("Vegetable", ignoreCase = true) }.sumOf { it.amount }
        val todayOtherExpenses = (todayExpenses - todayGroceryExpenses).coerceAtLeast(0.0)

        val todaySodaIncome = todayTxns.filter { it.type == "INCOME" && (it.category.contains("Soda", ignoreCase = true) || it.title.contains("Soda", ignoreCase = true)) }
            .sumOf { it.amount }
        val todayOtherIncome = todayTxns.filter { it.type == "INCOME" && !it.category.contains("Soda", ignoreCase = true) && !it.title.contains("Soda", ignoreCase = true) }
            .sumOf { it.amount }
        val todayTotalIncome = todaySodaIncome + todayOtherIncome

        val budgetLimit = budget?.dailyBudgetLimit ?: 800.0
        val remaining = budgetLimit - todayExpenses
        val usagePercent = if (budgetLimit > 0) ((todayExpenses / budgetLimit) * 100).toInt() else 0

        // Weekly budget tracking
        val weekRange = getTimeRange(TimeFilter.THIS_WEEK) ?: Pair(0L, Long.MAX_VALUE)
        val weekExpenses = transactions.filter { it.dateMillis in weekRange.first..weekRange.second && it.type == "EXPENSE" }
            .sumOf { it.amount }
        val weeklyLimit = budget?.weeklyBudgetLimit ?: 5000.0
        val remainingWeekly = weeklyLimit - weekExpenses
        val weeklyUsagePercent = if (weeklyLimit > 0) ((weekExpenses / weeklyLimit) * 100).toInt() else 0

        TodayDashboardSummary(
            todayExpenses = todayExpenses,
            todayGroceryExpenses = todayGroceryExpenses,
            todayOtherExpenses = todayOtherExpenses,
            todaySodaIncome = todaySodaIncome,
            todayOtherIncome = todayOtherIncome,
            todayTotalIncome = todayTotalIncome,
            dailyBudgetLimit = budgetLimit,
            remainingBudget = remaining,
            budgetUsagePercent = usagePercent,
            netDailyBalance = todayTotalIncome - todayExpenses,
            todayTransactionsCount = todayTxns.size,
            todayExpenseItemsCount = todayExpenseTxns.size,
            weeklyBudgetLimit = weeklyLimit,
            weeklyExpenses = weekExpenses,
            weeklyRemainingBudget = remainingWeekly,
            weeklyBudgetUsagePercent = weeklyUsagePercent
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TodayDashboardSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 800.0, 800.0, 0, 0.0, 0, 0, 5000.0, 0.0, 5000.0, 0)
    )

    // Chronological list of recent grocery expenses from Room database
    val recentGroceryExpenses: StateFlow<List<ExpenseIncomeEntity>> = repository.allTransactions
        .combine(MutableStateFlow(Unit)) { transactions, _ ->
            transactions.filter {
                it.type.equals("EXPENSE", ignoreCase = true) &&
                        (it.category.contains("Grocer", ignoreCase = true) ||
                                it.category.contains("Vegetable", ignoreCase = true) ||
                                it.category.contains("Food", ignoreCase = true))
            }.sortedByDescending { it.dateMillis }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Weekly day-by-day trends for the chart (Mon - Sun of current week)
    val weeklyTrends: StateFlow<List<WeeklyDayTrend>> = repository.allTransactions.combine(
        MutableStateFlow(Unit)
    ) { transactions, _ ->
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        val todayCal = Calendar.getInstance()

        val trends = mutableListOf<WeeklyDayTrend>()
        for (i in 0 until 7) {
            val dayStart = cal.timeInMillis
            val isToday = cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                    cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR)

            val dayEndCal = cal.clone() as Calendar
            dayEndCal.set(Calendar.HOUR_OF_DAY, 23)
            dayEndCal.set(Calendar.MINUTE, 59)
            dayEndCal.set(Calendar.SECOND, 59)
            dayEndCal.set(Calendar.MILLISECOND, 999)
            val dayEnd = dayEndCal.timeInMillis

            val dayTxns = transactions.filter { it.dateMillis in dayStart..dayEnd }
            val expenses = dayTxns.filter { it.type == "EXPENSE" }.sumOf { it.amount }
            val sodaIncome = dayTxns.filter { it.type == "INCOME" && (it.category.contains("Soda", ignoreCase = true) || it.title.contains("Soda", ignoreCase = true)) }
                .sumOf { it.amount }
            val totalIncome = dayTxns.filter { it.type == "INCOME" }.sumOf { it.amount }

            trends.add(
                WeeklyDayTrend(
                    dayName = dayFormat.format(Date(dayStart)),
                    dateDisplay = dateFormat.format(Date(dayStart)),
                    startOfDayMillis = dayStart,
                    endOfDayMillis = dayEnd,
                    expenseAmount = expenses,
                    sodaIncomeAmount = sodaIncome,
                    totalIncomeAmount = totalIncome,
                    isToday = isToday
                )
            )

            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        trends
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val defaultFrequentItems = listOf(
        FrequentPurchaseItem("Tomatoes (1 kg)", "தக்காளி", "Groceries", 40.0, "tomato", 5),
        FrequentPurchaseItem("Onions (1 kg)", "வெங்காயம்", "Groceries", 45.0, "onion", 5),
        FrequentPurchaseItem("Milk Packet (500ml)", "பால்", "Groceries", 30.0, "milk", 4),
        FrequentPurchaseItem("Goli Soda Ice Block", "ஐஸ் கட்டி", "Goli Soda Business", 50.0, "ice", 4),
        FrequentPurchaseItem("Fresh Curd (500g)", "தயிர்", "Groceries", 25.0, "curd", 3),
        FrequentPurchaseItem("Tea & Morning Snacks", "டீ & ஸ்நாக்ஸ்", "Entertainment", 25.0, "tea", 3),
        FrequentPurchaseItem("Cooking Oil (1 L)", "சமையல் எண்ணெய்", "Groceries", 140.0, "oil", 2),
        FrequentPurchaseItem("Bus / Share Auto", "பஸ் / ஆட்டோ", "Transportation", 30.0, "transport", 2)
    )

    // Dynamic frequent purchases based on actual transaction history + preset defaults
    val frequentPurchases: StateFlow<List<FrequentPurchaseItem>> = repository.allTransactions
        .combine(MutableStateFlow(Unit)) { transactions, _ ->
            val expenseTxns = transactions.filter { it.type == "EXPENSE" }
            if (expenseTxns.isEmpty()) {
                defaultFrequentItems
            } else {
                val grouped = expenseTxns.groupBy { it.title.trim().lowercase(Locale.getDefault()) }
                    .mapValues { entry ->
                        val count = entry.value.size
                        val latest = entry.value.maxByOrNull { it.dateMillis } ?: entry.value.first()
                        FrequentPurchaseItem(
                            title = latest.title,
                            nativeName = "",
                            category = latest.category,
                            amount = latest.amount,
                            iconKey = when {
                                latest.category.contains("Soda", ignoreCase = true) -> "soda"
                                latest.title.contains("Tomato", ignoreCase = true) -> "tomato"
                                latest.title.contains("Onion", ignoreCase = true) -> "onion"
                                latest.title.contains("Milk", ignoreCase = true) -> "milk"
                                latest.title.contains("Curd", ignoreCase = true) -> "curd"
                                latest.title.contains("Ice", ignoreCase = true) -> "ice"
                                latest.title.contains("Oil", ignoreCase = true) -> "oil"
                                latest.category.contains("Transport", ignoreCase = true) -> "transport"
                                else -> "grocery"
                            },
                            frequency = count
                        )
                    }
                    .values
                    .sortedByDescending { it.frequency }
                    .take(6)

                val result = grouped.toMutableList()
                defaultFrequentItems.forEach { def ->
                    if (result.none { it.title.equals(def.title, ignoreCase = true) || it.title.contains(def.title.take(5), ignoreCase = true) }) {
                        result.add(def)
                    }
                }
                result.take(8)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), defaultFrequentItems)

    // Spending Insights comparing Current Week vs Previous Week
    val spendingInsights: StateFlow<WeekComparisonData> = repository.allTransactions
        .combine(MutableStateFlow(Unit)) { transactions, _ ->
            val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())

            // Current Week: Monday 00:00 to now
            val currStartCal = Calendar.getInstance().apply {
                firstDayOfWeek = Calendar.MONDAY
                set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val currEndCal = (currStartCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, 6)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }

            // Previous Week: Monday 7 days prior
            val prevStartCal = (currStartCal.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, -7)
            }
            val prevEndCal = (currStartCal.clone() as Calendar).apply {
                add(Calendar.MILLISECOND, -1)
            }

            val currTxns = transactions.filter { it.dateMillis in currStartCal.timeInMillis..currEndCal.timeInMillis && it.type == "EXPENSE" }
            val prevTxns = transactions.filter { it.dateMillis in prevStartCal.timeInMillis..prevEndCal.timeInMillis && it.type == "EXPENSE" }

            val currGrocery = currTxns.filter { it.category.contains("Grocer", ignoreCase = true) }.sumOf { it.amount }
            val prevGrocery = prevTxns.filter { it.category.contains("Grocer", ignoreCase = true) }.sumOf { it.amount }
            val groceryDiff = currGrocery - prevGrocery
            val groceryPct = if (prevGrocery > 0) ((groceryDiff / prevGrocery) * 100) else 0.0

            val currBusiness = currTxns.filter { it.category.contains("Soda", ignoreCase = true) || it.category.contains("Business", ignoreCase = true) }.sumOf { it.amount }
            val prevBusiness = prevTxns.filter { it.category.contains("Soda", ignoreCase = true) || it.category.contains("Business", ignoreCase = true) }.sumOf { it.amount }
            val businessDiff = currBusiness - prevBusiness
            val businessPct = if (prevBusiness > 0) ((businessDiff / prevBusiness) * 100) else 0.0

            val currTotal = currTxns.sumOf { it.amount }
            val prevTotal = prevTxns.sumOf { it.amount }
            val totalDiff = currTotal - prevTotal
            val totalPct = if (prevTotal > 0) ((totalDiff / prevTotal) * 100) else 0.0

            val tips = mutableListOf<String>()
            if (groceryDiff < 0) {
                tips.add("🎉 Grocery Savings: You spent ₹${(-groceryDiff).toInt()} less on groceries this week vs last week!")
            } else if (groceryDiff > 0 && prevGrocery > 0) {
                tips.add("💡 Grocery Watch: Household food spend rose ₹${groceryDiff.toInt()} (+${groceryPct.toInt()}%). Sticking to weekly seasonal produce can help reduce this.")
            } else {
                tips.add("✅ Steady Grocery Spending: Your grocery budget is consistent with normal weekly averages.")
            }

            if (businessDiff < 0) {
                tips.add("🧊 Goli Soda Efficiency: Business expenses were ₹${(-businessDiff).toInt()} lower this week.")
            } else if (businessDiff > 0 && prevBusiness > 0) {
                tips.add("📈 Soda Business Investment: Ice and flavor expenses grew by ₹${businessDiff.toInt()}. Target peak afternoon hours to optimize revenue margin.")
            }

            if (totalDiff < 0) {
                tips.add("💰 Total Savings: Overall expenses dropped by ₹${(-totalDiff).toInt()} (-${(-totalPct).toInt()}%). Excellent budgeting discipline!")
            }

            WeekComparisonData(
                currentWeekGrocery = currGrocery,
                previousWeekGrocery = prevGrocery,
                groceryDiff = groceryDiff,
                groceryPercentChange = groceryPct,
                currentWeekBusiness = currBusiness,
                previousWeekBusiness = prevBusiness,
                businessDiff = businessDiff,
                businessPercentChange = businessPct,
                currentWeekTotal = currTotal,
                previousWeekTotal = prevTotal,
                totalDiff = totalDiff,
                totalPercentChange = totalPct,
                savingsTips = tips,
                currentWeekDateRange = "${dateFormat.format(currStartCal.time)} - ${dateFormat.format(currEndCal.time)}",
                previousWeekDateRange = "${dateFormat.format(prevStartCal.time)} - ${dateFormat.format(prevEndCal.time)}"
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            WeekComparisonData(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, emptyList(), "This Week", "Last Week")
        )

    fun logQuickPurchase(item: FrequentPurchaseItem, onSuccess: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = ExpenseIncomeEntity(
                type = "EXPENSE",
                category = item.category,
                title = item.title,
                amount = item.amount,
                dateMillis = System.currentTimeMillis(),
                note = "⚡ Single-tap quick add from Frequent Purchases"
            )
            repository.insertTransaction(entity)
            launch(Dispatchers.Main) {
                onSuccess("Added ₹${item.amount.toInt()} for ${item.title}")
            }
        }
    }

    fun updateDailyBudget(newLimit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDailyBudget(newLimit)
        }
    }

    fun updateWeeklyBudget(newLimit: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateWeeklyBudget(newLimit)
        }
    }

    fun setTimeFilter(filter: TimeFilter) {
        _timeFilter.value = filter
    }

    fun setTypeFilter(type: String) {
        _typeFilter.value = type
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun addTransaction(
        type: String, // "EXPENSE" or "INCOME"
        category: String,
        title: String,
        amount: Double,
        note: String = "",
        dateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = ExpenseIncomeEntity(
                type = type,
                category = category,
                title = title,
                amount = amount,
                dateMillis = dateMillis,
                note = note
            )
            repository.insertTransaction(entity)
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(id)
        }
    }

    fun addCustomCategory(name: String, isIncome: Boolean, iconKey: String = "category") {
        viewModelScope.launch(Dispatchers.IO) {
            val cat = CustomCategoryEntity(
                name = name.trim(),
                isIncome = isIncome,
                iconKey = iconKey
            )
            repository.addCustomCategory(cat)
        }
    }

    fun deleteCustomCategory(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCustomCategory(id)
        }
    }

    private fun getTimeRange(filter: TimeFilter): Pair<Long, Long>? {
        if (filter == TimeFilter.ALL_TIME) return null

        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val start = when (filter) {
            TimeFilter.TODAY -> cal.timeInMillis
            TimeFilter.THIS_WEEK -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.timeInMillis
            }
            TimeFilter.THIS_MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.timeInMillis
            }
            TimeFilter.ALL_TIME -> 0L
        }

        val endCal = Calendar.getInstance()
        endCal.set(Calendar.HOUR_OF_DAY, 23)
        endCal.set(Calendar.MINUTE, 59)
        endCal.set(Calendar.SECOND, 59)
        endCal.set(Calendar.MILLISECOND, 999)
        val end = endCal.timeInMillis

        return Pair(start, end)
    }
}
