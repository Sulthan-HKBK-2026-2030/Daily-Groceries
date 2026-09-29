package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.ui.components.AddCategoryDialog
import com.example.ui.components.AddTransactionDialog
import com.example.ui.components.ItemIconBadge
import com.example.ui.viewmodel.CashbookViewModel
import com.example.ui.viewmodel.TimeFilter
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CashbookScreen(
    cashbookViewModel: CashbookViewModel,
    onNavigateToAddExpense: (category: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val filteredTransactions by cashbookViewModel.filteredTransactions.collectAsStateWithLifecycle()
    val summary by cashbookViewModel.currentSummary.collectAsStateWithLifecycle()
    val timeFilter by cashbookViewModel.timeFilter.collectAsStateWithLifecycle()
    val typeFilter by cashbookViewModel.typeFilter.collectAsStateWithLifecycle()
    val selectedCategoryFilter by cashbookViewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val customCategories by cashbookViewModel.customCategories.collectAsStateWithLifecycle()

    val (totalIncome, totalExpense, netSavings) = summary

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var addDialogInitialType by remember { mutableStateOf("EXPENSE") }
    var showCategoryDialog by remember { mutableStateOf(false) }

    val allCategoryNames = remember(customCategories) {
        val base = listOf("ALL") + cashbookViewModel.defaultExpenseCategories + cashbookViewModel.defaultIncomeCategories
        (base + customCategories.map { it.name }).distinct()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onNavigateToAddExpense("Groceries")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💰 Daily Cashbook & Wages",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Track Family Expenses & Daily Soda / Labor Income",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showCategoryDialog = true },
                        modifier = Modifier.testTag("manage_categories_button")
                    ) {
                        Icon(imageVector = Icons.Default.Category, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Category")
                    }
                }
            }

            // Summary Card (Income, Spent, Balance)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Net Balance in Hand
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Net Cash Balance (${timeFilter.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.0f", netSavings)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (netSavings >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (netSavings >= 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ) {
                                Text(
                                    text = if (netSavings >= 0) "Surplus (+)" else "Deficit (-)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netSavings >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Income vs Spent row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Total Income",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "+₹${totalIncome.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "Total Spent",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "-₹${totalExpense.toInt()}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFC62828)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick Entry Buttons (Add Expense / Add Wages)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            onNavigateToAddExpense("Groceries")
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("quick_add_expense_btn")
                    ) {
                        Text("- Add Expense", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            addDialogInitialType = "INCOME"
                            showAddDialog = true
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("quick_add_income_btn")
                    ) {
                        Text("+ Add Income / Wages", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Time Filter Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filters = listOf(
                        TimeFilter.TODAY to "Today",
                        TimeFilter.THIS_WEEK to "This Week",
                        TimeFilter.THIS_MONTH to "This Month",
                        TimeFilter.ALL_TIME to "All Time"
                    )
                    filters.forEach { (tf, label) ->
                        val isSel = timeFilter == tf
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { cashbookViewModel.setTimeFilter(tf) }
                                .weight(1f)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Category Filter Row (Filter chips: All, Groceries, Utilities, Wages, etc.)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cashbook_category_filter_chips_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allCategoryNames) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        val tag = when {
                            cat.equals("ALL", ignoreCase = true) -> "cashbook_filter_chip_all"
                            cat.equals("Groceries", ignoreCase = true) -> "cashbook_filter_chip_groceries"
                            cat.equals("Utilities", ignoreCase = true) -> "cashbook_filter_chip_utilities"
                            cat.equals("Wages", ignoreCase = true) -> "cashbook_filter_chip_wages"
                            else -> "cashbook_filter_chip_${cat.lowercase().replace(" ", "_")}"
                        }
                        FilterChip(
                            selected = isSelected,
                            onClick = { cashbookViewModel.setCategoryFilter(cat) },
                            label = {
                                Text(
                                    text = if (cat == "ALL") "All Categories" else cat,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag(tag)
                        )
                    }
                }
            }

            // Transactions Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Transactions (${filteredTransactions.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // Type filter toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("ALL", "EXPENSE", "INCOME").forEach { type ->
                            val isSel = typeFilter == type
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier.clickable { cashbookViewModel.setTypeFilter(type) }
                            ) {
                                Text(
                                    text = if (type == "ALL") "All" else if (type == "EXPENSE") "Expenses" else "Income",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Transaction Cards
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "📝", fontSize = 40.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No records for this selection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Tap + to add an expense or daily wage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { txn ->
                    TransactionItemCard(
                        transaction = txn,
                        onDelete = {
                            cashbookViewModel.deleteTransaction(txn.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Deleted ${txn.title}")
                            }
                        }
                    )
                }
            }
        }
    }

    // Add Transaction Dialog
    if (showAddDialog) {
        AddTransactionDialog(
            initialType = addDialogInitialType,
            defaultCategories = if (addDialogInitialType == "EXPENSE") cashbookViewModel.defaultExpenseCategories else cashbookViewModel.defaultIncomeCategories,
            customCategories = customCategories,
            onDismiss = { showAddDialog = false },
            onSave = { type, category, title, amount, note ->
                cashbookViewModel.addTransaction(type, category, title, amount, note)
                scope.launch {
                    snackbarHostState.showSnackbar("Added $type: $title (₹${amount.toInt()})")
                }
            },
            onOpenAddCategory = { showCategoryDialog = true }
        )
    }

    // Add Category Dialog
    if (showCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onSaveCategory = { name, isIncome ->
                cashbookViewModel.addCustomCategory(name, isIncome)
                scope.launch {
                    snackbarHostState.showSnackbar("Added category: $name")
                }
            }
        )
    }
}

@Composable
fun TransactionItemCard(
    transaction: ExpenseIncomeEntity,
    onDelete: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
    val dateStr = dateFormat.format(Date(transaction.dateMillis))

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemIconBadge(
                iconKey = when (transaction.category) {
                    "Groceries" -> "groceries"
                    "Goli Soda Business", "Goli Soda Sales" -> "soda"
                    "Household Bills" -> "bills"
                    "Transportation" -> "transport"
                    "Entertainment" -> "entertainment"
                    "Personal Care" -> "personal_care"
                    "Daily Wages / Salary" -> "wages"
                    else -> "misc"
                },
                category = transaction.category,
                size = 44.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                if (transaction.note.isNotBlank()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isIncome) "+₹${transaction.amount.toInt()}" else "-₹${transaction.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
