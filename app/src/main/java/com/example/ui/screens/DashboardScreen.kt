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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CsvExportDialog
import com.example.ui.components.ItemIconBadge
import com.example.ui.components.RecentGroceryExpensesList
import com.example.ui.components.SettingsDialog
import com.example.ui.components.SpendingInsightsDialog
import com.example.ui.components.WeeklySpendingTrendChart
import com.example.ui.viewmodel.CashbookViewModel
import com.example.ui.viewmodel.FrequentPurchaseItem
import com.example.ui.viewmodel.SodaViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    cashbookViewModel: CashbookViewModel,
    sodaViewModel: SodaViewModel,
    onNavigateToGroceries: () -> Unit,
    onNavigateToSoda: () -> Unit,
    onNavigateToCashbook: () -> Unit,
    onNavigateToChecklist: () -> Unit,
    onNavigateToAddExpense: (category: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by cashbookViewModel.todayDashboardSummary.collectAsStateWithLifecycle()
    val weeklyTrends by cashbookViewModel.weeklyTrends.collectAsStateWithLifecycle()
    val recentTransactions by cashbookViewModel.filteredTransactions.collectAsStateWithLifecycle()
    val recentGroceryExpenses by cashbookViewModel.recentGroceryExpenses.collectAsStateWithLifecycle()
    val allTransactions by cashbookViewModel.allTransactions.collectAsStateWithLifecycle()
    val allSodaSales by sodaViewModel.allSales.collectAsStateWithLifecycle()
    val sodaPrice by sodaViewModel.pricePerBottle.collectAsStateWithLifecycle()
    val frequentPurchases by cashbookViewModel.frequentPurchases.collectAsStateWithLifecycle()
    val spendingInsights by cashbookViewModel.spendingInsights.collectAsStateWithLifecycle()

    var showBudgetDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showInsightsDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val todayDateFormatted = remember {
        SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Welcome Header with CSV Export Button
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "👋 Vanakkam, Sulthan!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = todayDateFormatted,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { showExportDialog = true },
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("dashboard_export_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV", style = MaterialTheme.typography.labelSmall)
                        }

                        IconButton(
                            onClick = { showSettingsDialog = true },
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("dashboard_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // ⚡ FREQUENT PURCHASES (Single-Tap Log)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "⚡", fontSize = 18.sp)
                            Text(
                                text = "Frequent Purchases",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "Single-tap to log",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(frequentPurchases) { item ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .width(135.dp)
                                    .clickable {
                                        cashbookViewModel.logQuickPurchase(item) { msg ->
                                            scope.launch {
                                                snackbarHostState.showSnackbar(msg)
                                            }
                                        }
                                    }
                                    .testTag("frequent_purchase_${item.title}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    ItemIconBadge(
                                        iconKey = item.iconKey,
                                        category = item.category,
                                        size = 36.dp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )

                                    if (item.nativeName.isNotBlank()) {
                                        Text(
                                            text = item.nativeName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp,
                                            maxLines = 1
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    ) {
                                        Text(
                                            text = "+ ₹${item.amount.toInt()}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 📊 SPENDING INSIGHTS VS PREVIOUS WEEK BANNER
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInsightsDialog = true }
                        .testTag("dashboard_spending_insights_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Insights,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "📊 Spending Insights",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                val isSaved = spendingInsights.totalDiff < 0
                                Text(
                                    text = if (isSaved)
                                        "Saved ₹${(-spendingInsights.totalDiff).toInt()} vs last week! (Groceries: ₹${spendingInsights.currentWeekGrocery.toInt()})"
                                    else if (spendingInsights.totalDiff > 0)
                                        "+₹${spendingInsights.totalDiff.toInt()} vs last week (Groceries: ₹${spendingInsights.currentWeekGrocery.toInt()})"
                                    else
                                        "Groceries: ₹${spendingInsights.currentWeekGrocery.toInt()} | Business: ₹${spendingInsights.currentWeekBusiness.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSaved) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        FilledTonalButton(
                            onClick = { showInsightsDialog = true },
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Compare", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 1. WEEKLY SPENDING LIMIT & PROGRESS BAR CARD (User's New Feature Requirement!)
            item {
                val isWeekOverBudget = summary.weeklyRemainingBudget < 0
                val weekProgress = if (summary.weeklyBudgetLimit > 0) {
                    (summary.weeklyExpenses / summary.weeklyBudgetLimit).toFloat().coerceIn(0f, 1f)
                } else 0f

                val weekProgressColor = when {
                    isWeekOverBudget -> Color(0xFFC62828)
                    summary.weeklyBudgetUsagePercent >= 85 -> Color(0xFFF57C00)
                    summary.weeklyBudgetUsagePercent >= 60 -> Color(0xFFFBC02D)
                    else -> Color(0xFF00897B)
                }

                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isWeekOverBudget)
                            Color(0xFFFFEBEE)
                        else
                            MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_weekly_budget_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "📅", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Weekly Spending Goal",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Limit: ₹${summary.weeklyBudgetLimit.toInt()} / week",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showBudgetDialog = true },
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("edit_weekly_goal_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Set Limit", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large Remaining Amount vs Actual Spent Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isWeekOverBudget) "Exceeded Weekly Goal by" else "Remaining for this Week",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isWeekOverBudget) Color(0xFFC62828) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.0f", if (isWeekOverBudget) -summary.weeklyRemainingBudget else summary.weeklyRemainingBudget)}",
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWeekOverBudget) Color(0xFFC62828) else Color(0xFF006D5B)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isWeekOverBudget) Color(0xFFFFCDD2) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = if (isWeekOverBudget) "Over Limit ⚠️" else "${summary.weeklyBudgetUsagePercent}% Used",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWeekOverBudget) Color(0xFFB71C1C) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Tracking Progress Bar
                        LinearProgressIndicator(
                            progress = { weekProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = weekProgressColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Actual Spent: ₹${summary.weeklyExpenses.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Weekly Goal: ₹${summary.weeklyBudgetLimit.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // 2. DAILY BUDGET QUICK-VIEW CARD (Material 3 Card)
            item {
                val isDailyOver = summary.remainingBudget < 0
                val dailyProgress = if (summary.dailyBudgetLimit > 0) {
                    (summary.todayExpenses / summary.dailyBudgetLimit).toFloat().coerceIn(0f, 1f)
                } else 0f

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dashboard_daily_budget_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎯", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Daily Spending Budget",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = "Goal: ₹${summary.dailyBudgetLimit.toInt()}/day",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isDailyOver) "Daily Overrun" else "Remaining Today",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.0f", if (isDailyOver) -summary.remainingBudget else summary.remainingBudget)}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDailyOver) Color(0xFFC62828) else Color(0xFF006D5B)
                                )
                            }

                            Text(
                                text = "Spent ₹${summary.todayExpenses.toInt()}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { dailyProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isDailyOver) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 3. DUAL SUMMARY CARDS: Daily Expenses vs Total Goli Soda Income (Material 3 Cards)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card A: Daily Expenses Summary
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_expenses_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "💸 Spent Today",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFFE53935), CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "₹${summary.todayExpenses.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFC62828)
                            )

                            Text(
                                text = "${summary.todayTransactionsCount} purchases logged",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToAddExpense("Groceries") }
                            ) {
                                Text(
                                    text = "+ Add Expense",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Card B: Total Income from Goli Soda Sales
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_soda_income_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🍾 Soda Income",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF00897B), CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "₹${summary.todaySodaIncome.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF006D5B)
                            )

                            val estimatedBottles = if (sodaPrice > 0) (summary.todaySodaIncome / sodaPrice).toInt() else 0
                            Text(
                                text = "$estimatedBottles bottles sold (@ ₹${sodaPrice.toInt()})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigateToSoda() }
                            ) {
                                Text(
                                    text = "Open Counter",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Net Daily Balance / Hand Cash Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Today's Net Hand Cash",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Soda & Wages Income - Expenses",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = if (summary.netDailyBalance >= 0)
                                "+₹${summary.netDailyBalance.toInt()}"
                            else
                                "-₹${(-summary.netDailyBalance).toInt()}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (summary.netDailyBalance >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            // 5. WEEKLY SPENDING & SODA INCOME TREND CHART
            item {
                WeeklySpendingTrendChart(
                    trends = weeklyTrends
                )
            }

            // 6. Quick Shortcuts Grid
            item {
                Text(
                    text = "⚡ Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionTile(
                        icon = "🛒",
                        title = "Buy Groceries",
                        subtitle = "100g/250g/1kg",
                        onClick = onNavigateToGroceries,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionTile(
                        icon = "🍾",
                        title = "Goli Soda",
                        subtitle = "+1, +5, Crates",
                        onClick = onNavigateToSoda,
                        modifier = Modifier.weight(1f)
                    )

                    QuickActionTile(
                        icon = "📋",
                        title = "Pantry List",
                        subtitle = "Monthly Stock",
                        onClick = onNavigateToChecklist,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 7. Recent Expenses List with Category Filter Chips
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🕒 Recent Expenses & Purchases",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Filter by Groceries, Utilities, Wages, etc.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = onNavigateToCashbook) {
                        Text("Cashbook (${allTransactions.size})")
                    }
                }
            }

            item {
                RecentGroceryExpensesList(
                    expenses = allTransactions.filter { it.type == "EXPENSE" || it.category.contains("Wage", ignoreCase = true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 460.dp),
                    onExpenseClick = { onNavigateToCashbook() }
                )
            }
        }
    }

    // Set Daily & Weekly Budget Targets Dialog
    if (showBudgetDialog) {
        var dailyInput by remember { mutableStateOf(summary.dailyBudgetLimit.toInt().toString()) }
        var weeklyInput by remember { mutableStateOf(summary.weeklyBudgetLimit.toInt().toString()) }

        AlertDialog(
            onDismissRequest = { showBudgetDialog = false },
            title = { Text("Budget & Spending Limits") },
            text = {
                Column {
                    Text(
                        text = "Define your planned daily budget and weekly spending limits:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "1. Weekly Spending Limit",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = weeklyInput,
                        onValueChange = { weeklyInput = it },
                        label = { Text("Weekly Limit (₹)") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weekly_budget_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(3000, 5000, 7500, 10000).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { weeklyInput = preset.toString() }
                            ) {
                                Text(
                                    text = "₹$preset",
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "2. Daily Spending Target",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = dailyInput,
                        onValueChange = { dailyInput = it },
                        label = { Text("Daily Target (₹)") },
                        prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("daily_budget_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(500, 800, 1000, 1500).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { dailyInput = preset.toString() }
                            ) {
                                Text(
                                    text = "₹$preset",
                                    style = MaterialTheme.typography.labelSmall,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val dLimit = dailyInput.toDoubleOrNull()
                        val wLimit = weeklyInput.toDoubleOrNull()
                        if (dLimit != null && dLimit > 0) {
                            cashbookViewModel.updateDailyBudget(dLimit)
                        }
                        if (wLimit != null && wLimit > 0) {
                            cashbookViewModel.updateWeeklyBudget(wLimit)
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("Updated spending limits successfully!")
                        }
                        showBudgetDialog = false
                    },
                    enabled = (dailyInput.toDoubleOrNull() ?: 0.0) > 0 && (weeklyInput.toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Text("Save Goals")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // CSV Export Dialog
    if (showExportDialog) {
        CsvExportDialog(
            transactions = allTransactions,
            sodaSales = allSodaSales,
            onDismiss = { showExportDialog = false },
            onCopied = {
                scope.launch {
                    snackbarHostState.showSnackbar("CSV data copied to clipboard!")
                }
            }
        )
    }

    // App Settings Dialog (Theme, Reminders & Cloud Sync)
    if (showSettingsDialog) {
        SettingsDialog(
            onDismiss = { showSettingsDialog = false }
        )
    }

    // Spending Insights Dialog (Current Week vs Previous Week)
    if (showInsightsDialog) {
        SpendingInsightsDialog(
            data = spendingInsights,
            onDismiss = { showInsightsDialog = false }
        )
    }
}

@Composable
fun QuickActionTile(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
