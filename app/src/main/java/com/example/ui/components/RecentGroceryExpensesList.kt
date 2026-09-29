package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ExpenseIncomeEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Filterable Expense List View.
 * Displays a chronological list of recent expenses from the database,
 * featuring interactive M3 FilterChips that allow users to toggle visibility
 * of expenses based on categories like 'Groceries', 'Utilities', or 'Wages'.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentGroceryExpensesList(
    groceryExpenses: List<ExpenseIncomeEntity> = emptyList(),
    modifier: Modifier = Modifier,
    expenses: List<ExpenseIncomeEntity> = groceryExpenses,
    initialCategories: Set<String> = emptySet(),
    onExpenseClick: ((ExpenseIncomeEntity) -> Unit)? = null
) {
    val effectiveExpenses = if (expenses.isNotEmpty()) expenses else groceryExpenses
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val fullDateFormat = remember { SimpleDateFormat("d MMM yyyy, h:mm a", Locale.getDefault()) }

    // Preset prominent categories as required by the user, plus any dynamic ones
    val predefinedCategories = listOf("All", "Groceries", "Utilities", "Wages", "Goli Soda Business", "Transportation")
    val allCategories = remember(effectiveExpenses) {
        val extra = effectiveExpenses.map { it.category.trim() }
            .filter { it.isNotBlank() && !predefinedCategories.any { p -> p.equals(it, ignoreCase = true) } }
            .distinct()
        (predefinedCategories + extra).distinct()
    }

    // Set of currently active category filters. If empty or contains "All", all are visible.
    var selectedCategories by remember {
        mutableStateOf(if (initialCategories.isNotEmpty()) initialCategories else setOf("All"))
    }

    fun isItemMatchingCategory(item: ExpenseIncomeEntity, filterKey: String): Boolean {
        val cat = item.category.trim()
        val title = item.title.trim().lowercase(Locale.getDefault())
        return when {
            filterKey.equals("All", ignoreCase = true) -> true
            filterKey.equals("Groceries", ignoreCase = true) ->
                cat.contains("Grocer", ignoreCase = true) ||
                        cat.contains("Vegetable", ignoreCase = true) ||
                        cat.contains("Provision", ignoreCase = true) ||
                        cat.contains("Food", ignoreCase = true) ||
                        cat.equals("Groceries", ignoreCase = true)
            filterKey.equals("Utilities", ignoreCase = true) ->
                cat.contains("Utilit", ignoreCase = true) ||
                        cat.contains("Bill", ignoreCase = true) ||
                        cat.contains("Electricity", ignoreCase = true) ||
                        cat.contains("Power", ignoreCase = true) ||
                        cat.contains("Water", ignoreCase = true) ||
                        cat.contains("Gas", ignoreCase = true) ||
                        title.contains("bill") || title.contains("electric") || title.contains("gas")
            filterKey.equals("Wages", ignoreCase = true) ->
                cat.contains("Wage", ignoreCase = true) ||
                        cat.contains("Salary", ignoreCase = true) ||
                        cat.contains("Labor", ignoreCase = true) ||
                        title.contains("wage") || title.contains("salary") || title.contains("coolie") || title.contains("helper")
            filterKey.equals("Goli Soda Business", ignoreCase = true) || filterKey.equals("Goli Soda", ignoreCase = true) ->
                cat.contains("Soda", ignoreCase = true) || title.contains("soda")
            filterKey.equals("Transportation", ignoreCase = true) ->
                cat.contains("Transport", ignoreCase = true) || cat.contains("Travel", ignoreCase = true) || title.contains("auto") || title.contains("petrol")
            else -> cat.equals(filterKey, ignoreCase = true)
        }
    }

    // Toggle category visibility
    fun toggleCategory(cat: String) {
        if (cat.equals("All", ignoreCase = true)) {
            selectedCategories = setOf("All")
        } else {
            val nonAll = selectedCategories.filter { !it.equals("All", ignoreCase = true) }.toSet()
            if (nonAll.contains(cat)) {
                val updated = nonAll - cat
                selectedCategories = if (updated.isEmpty()) setOf("All") else updated
            } else {
                selectedCategories = nonAll + cat
            }
        }
    }

    // Filter items according to the active toggled categories
    val isAllActive = selectedCategories.isEmpty() || selectedCategories.contains("All")
    val filteredList = remember(effectiveExpenses, selectedCategories, isAllActive) {
        val list = if (isAllActive) {
            effectiveExpenses
        } else {
            effectiveExpenses.filter { item ->
                selectedCategories.any { catFilter -> isItemMatchingCategory(item, catFilter) }
            }
        }
        list.sortedByDescending { it.dateMillis }
    }

    // Count badges per category
    val categoryCounts = remember(effectiveExpenses, allCategories) {
        val counts = mutableMapOf<String, Int>()
        counts["All"] = effectiveExpenses.size
        for (cat in allCategories) {
            if (!cat.equals("All", ignoreCase = true)) {
                counts[cat] = effectiveExpenses.count { isItemMatchingCategory(it, cat) }
            }
        }
        counts
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("expense_list_container")
    ) {
        // 1. FILTER CHIPS ROW: Toggle visibility for Groceries, Utilities, Wages, etc.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Filter by Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isAllActive) {
                Text(
                    text = "${selectedCategories.size} selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .testTag("expense_filter_chips_row"),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(allCategories) { cat ->
                val isSelected = if (cat.equals("All", ignoreCase = true)) {
                    isAllActive
                } else {
                    !isAllActive && selectedCategories.contains(cat)
                }

                val count = categoryCounts[cat] ?: 0

                val tag = when {
                    cat.equals("All", ignoreCase = true) -> "filter_chip_all"
                    cat.equals("Groceries", ignoreCase = true) -> "filter_chip_groceries"
                    cat.equals("Utilities", ignoreCase = true) -> "filter_chip_utilities"
                    cat.equals("Wages", ignoreCase = true) -> "filter_chip_wages"
                    else -> "filter_chip_${cat.lowercase(Locale.getDefault()).replace(" ", "_")}"
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { toggleCategory(cat) },
                    label = {
                        Text(
                            text = "$cat ($count)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
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
                    } else {
                        {
                            val emoji = when {
                                cat.equals("All", ignoreCase = true) -> "✨"
                                cat.contains("Grocer", ignoreCase = true) -> "🥬"
                                cat.contains("Utilit", ignoreCase = true) || cat.contains("Bill", ignoreCase = true) -> "💡"
                                cat.contains("Wage", ignoreCase = true) -> "💰"
                                cat.contains("Soda", ignoreCase = true) -> "🍾"
                                cat.contains("Transport", ignoreCase = true) -> "🛵"
                                else -> "🏷️"
                            }
                            Text(emoji, fontSize = 12.sp)
                        }
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when {
                            cat.contains("Grocer", ignoreCase = true) -> Color(0xFFE8F5E9)
                            cat.contains("Utilit", ignoreCase = true) -> Color(0xFFFFF8E1)
                            cat.contains("Wage", ignoreCase = true) -> Color(0xFFE0F2F1)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        selectedLabelColor = when {
                            cat.contains("Grocer", ignoreCase = true) -> Color(0xFF1B5E20)
                            cat.contains("Utilit", ignoreCase = true) -> Color(0xFFE65100)
                            cat.contains("Wage", ignoreCase = true) -> Color(0xFF004D40)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    ),
                    modifier = Modifier.testTag(tag)
                )
            }
        }

        // 2. EMPTY STATE
        if (filteredList.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_list_empty_card")
                    .testTag("recent_grocery_empty_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (effectiveExpenses.isEmpty()) "No expenses recorded yet" else "No expenses match the selected categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (effectiveExpenses.isEmpty())
                            "Log daily purchases like Groceries, Utilities, or Wages to see them here."
                        else
                            "Try toggling on other category filter chips above or reset to All.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    if (!isAllActive) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { selectedCategories = setOf("All") },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text("Reset to All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
            return
        }

        // 3. EXPENSE LIST LAZY COLUMN
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("expense_list_lazy_column")
                .testTag("recent_grocery_expenses_lazy_column"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredList,
                key = { it.id }
            ) { item ->
                val dateLabel = remember(item.dateMillis) {
                    formatChronologicalDate(item.dateMillis, timeFormat, fullDateFormat)
                }

                val iconKey = remember(item.title, item.category) {
                    inferIconKey(item.title, item.category)
                }

                val isWage = item.category.contains("Wage", ignoreCase = true) || item.title.contains("wage", ignoreCase = true)
                val isUtility = item.category.contains("Utilit", ignoreCase = true) || item.category.contains("Bill", ignoreCase = true)
                val isGrocery = item.category.contains("Grocer", ignoreCase = true) || item.category.contains("Vegetable", ignoreCase = true)

                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = onExpenseClick != null) { onExpenseClick?.invoke(item) }
                        .testTag("expense_item_${item.id}")
                        .testTag("grocery_expense_item_${item.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            ItemIconBadge(
                                iconKey = iconKey,
                                category = item.category,
                                size = 42.dp
                            )

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Text(
                                        text = dateLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    if (!item.note.isNullOrBlank()) {
                                        Text(
                                            text = "• ${item.note}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "₹${if (item.amount % 1.0 == 0.0) item.amount.toInt().toString() else String.format(Locale.getDefault(), "%.2f", item.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.type.equals("INCOME", ignoreCase = true)) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )

                            val badgeBg = when {
                                isGrocery -> Color(0xFFE8F5E9)
                                isUtility -> Color(0xFFFFF8E1)
                                isWage -> Color(0xFFE0F2F1)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            val badgeColor = when {
                                isGrocery -> Color(0xFF2E7D32)
                                isUtility -> Color(0xFFE65100)
                                isWage -> Color(0xFF00796B)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBg,
                                modifier = Modifier.padding(top = 3.dp)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Convenience Alias for ExpenseListView
 */
@Composable
fun ExpenseListView(
    expenses: List<ExpenseIncomeEntity>,
    modifier: Modifier = Modifier,
    initialCategories: Set<String> = emptySet(),
    onExpenseClick: ((ExpenseIncomeEntity) -> Unit)? = null
) {
    RecentGroceryExpensesList(
        groceryExpenses = expenses,
        modifier = modifier,
        expenses = expenses,
        initialCategories = initialCategories,
        onExpenseClick = onExpenseClick
    )
}

private fun inferIconKey(title: String, category: String): String {
    val t = title.lowercase(Locale.getDefault())
    val c = category.lowercase(Locale.getDefault())
    return when {
        t.contains("tomato") -> "tomato"
        t.contains("onion") -> "onion"
        t.contains("potato") -> "potato"
        t.contains("garlic") -> "garlic"
        t.contains("ginger") -> "ginger"
        t.contains("chilli") -> "green_chilli"
        t.contains("carrot") -> "carrot"
        t.contains("bean") -> "beans"
        t.contains("curry") -> "curry_leaves"
        t.contains("coriander") -> "coriander"
        t.contains("mint") -> "mint"
        t.contains("lemon") -> "lemon"
        t.contains("cabbage") -> "cabbage"
        t.contains("cauliflower") -> "cauliflower"
        t.contains("brinjal") -> "brinjal"
        t.contains("ladies_finger") || t.contains("okra") -> "ladies_finger"
        t.contains("cucumber") -> "cucumber"
        t.contains("beetroot") -> "beetroot"
        t.contains("drumstick") -> "drumstick"
        t.contains("rice") -> "rice"
        t.contains("wheat") || t.contains("atta") -> "wheat_atta"
        t.contains("dal") || t.contains("dhal") -> "toor_dal"
        t.contains("oil") -> "oil"
        t.contains("milk") -> "milk"
        t.contains("curd") -> "curd"
        t.contains("paneer") -> "paneer"
        t.contains("ghee") -> "ghee"
        t.contains("sugar") -> "sugar"
        t.contains("salt") -> "salt"
        t.contains("tea") -> "tea"
        t.contains("coffee") -> "coffee_powder"
        t.contains("egg") -> "eggs"
        t.contains("electric") || t.contains("power") || t.contains("tneb") || t.contains("bill") || t.contains("gas") || c.contains("utilit") || c.contains("bill") -> "bills"
        t.contains("wage") || t.contains("salary") || t.contains("labor") || t.contains("coolie") || t.contains("helper") || c.contains("wage") -> "wages"
        t.contains("soda") || t.contains("syrup") || t.contains("ice") || c.contains("soda") -> "soda"
        t.contains("transport") || t.contains("auto") || t.contains("bus") || t.contains("petrol") || c.contains("transport") -> "transport"
        else -> if (c.contains("grocer")) "grocery" else "misc"
    }
}

private fun formatChronologicalDate(
    dateMillis: Long,
    timeFormat: SimpleDateFormat,
    fullDateFormat: SimpleDateFormat
): String {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply { timeInMillis = dateMillis }

    val isSameDay = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    if (isSameDay) {
        return "Today, ${timeFormat.format(Date(dateMillis))}"
    }

    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

    if (isYesterday) {
        return "Yesterday, ${timeFormat.format(Date(dateMillis))}"
    }

    return fullDateFormat.format(Date(dateMillis))
}
