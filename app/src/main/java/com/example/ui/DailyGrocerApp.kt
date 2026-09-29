package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CashbookScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExpenseInputFormScreen
import com.example.ui.screens.GoliSodaScreen
import com.example.ui.screens.GroceriesScreen
import com.example.ui.screens.PantryChecklistScreen
import com.example.ui.viewmodel.CashbookViewModel
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SodaViewModel

enum class AppScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    DASHBOARD("Home", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    GROCERIES("Daily Buy", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
    GOLI_SODA("Goli Soda", Icons.Filled.LocalDrink, Icons.Outlined.LocalDrink),
    CASHBOOK("Cashbook", Icons.Filled.Payments, Icons.Outlined.Payments),
    CHECKLIST("Monthly List", Icons.Filled.Assignment, Icons.Outlined.Assignment)
}

@Composable
fun DailyGrocerApp(
    groceryViewModel: GroceryViewModel = viewModel(),
    sodaViewModel: SodaViewModel = viewModel(),
    cashbookViewModel: CashbookViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.DASHBOARD) }
    var activeExpenseCategory by remember { mutableStateOf<String?>(null) }

    val customCategories by cashbookViewModel.customCategories.collectAsStateWithLifecycle()
    val availableExpenseCategories = remember(customCategories) {
        val customExpense = customCategories.filter { !it.isIncome }.map { it.name }
        (cashbookViewModel.defaultExpenseCategories + customExpense).distinct()
    }

    // Back handling: if in expense form, back closes the form; if in secondary tab, back goes to Dashboard
    if (activeExpenseCategory != null) {
        BackHandler {
            activeExpenseCategory = null
        }
    } else if (currentScreen != AppScreen.DASHBOARD) {
        BackHandler {
            currentScreen = AppScreen.DASHBOARD
        }
    }

    if (activeExpenseCategory != null) {
        // Dedicated reusable expense input form screen
        ExpenseInputFormScreen(
            initialCategory = activeExpenseCategory ?: "Groceries",
            availableCategories = availableExpenseCategories,
            onSaveExpense = { category, title, amount, note, paymentMode, dateMillis ->
                cashbookViewModel.addTransaction(
                    type = "EXPENSE",
                    category = category,
                    title = title,
                    amount = amount,
                    note = note,
                    dateMillis = dateMillis
                )
                activeExpenseCategory = null
            },
            onCancel = {
                activeExpenseCategory = null
            }
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("app_navigation_bar")
                ) {
                    AppScreen.values().forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentScreen = screen },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = screen.title
                                )
                            },
                            label = { Text(text = screen.title) },
                            modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> {
                        DashboardScreen(
                            cashbookViewModel = cashbookViewModel,
                            sodaViewModel = sodaViewModel,
                            onNavigateToGroceries = { currentScreen = AppScreen.GROCERIES },
                            onNavigateToSoda = { currentScreen = AppScreen.GOLI_SODA },
                            onNavigateToCashbook = { currentScreen = AppScreen.CASHBOOK },
                            onNavigateToChecklist = { currentScreen = AppScreen.CHECKLIST },
                            onNavigateToAddExpense = { category ->
                                activeExpenseCategory = category
                            }
                        )
                    }
                    AppScreen.GROCERIES -> {
                        GroceriesScreen(
                            groceryViewModel = groceryViewModel,
                            onDirectExpenseAdd = { title, amount, note ->
                                cashbookViewModel.addTransaction(
                                    type = "EXPENSE",
                                    category = "Groceries",
                                    title = title,
                                    amount = amount,
                                    note = note
                                )
                            }
                        )
                    }
                    AppScreen.GOLI_SODA -> {
                        GoliSodaScreen(
                            sodaViewModel = sodaViewModel,
                            onNavigateToCashbook = { currentScreen = AppScreen.CASHBOOK },
                            onNavigateToAddExpense = { category ->
                                activeExpenseCategory = category
                            }
                        )
                    }
                    AppScreen.CASHBOOK -> {
                        CashbookScreen(
                            cashbookViewModel = cashbookViewModel,
                            onNavigateToAddExpense = { category ->
                                activeExpenseCategory = category
                            }
                        )
                    }
                    AppScreen.CHECKLIST -> {
                        PantryChecklistScreen(
                            groceryViewModel = groceryViewModel
                        )
                    }
                }
            }
        }
    }
}
