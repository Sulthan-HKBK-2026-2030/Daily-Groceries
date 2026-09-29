package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CustomCategoryEntity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddTransactionDialog(
    initialType: String = "EXPENSE",
    defaultCategories: List<String>,
    customCategories: List<CustomCategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (type: String, category: String, title: String, amount: Double, note: String) -> Unit,
    onOpenAddCategory: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(initialType) } // "EXPENSE" or "INCOME"
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    val availableCategories = remember(type, customCategories) {
        val customForType = customCategories
            .filter { if (type == "INCOME") it.isIncome else !it.isIncome }
            .map { it.name }
        (defaultCategories + customForType).distinct()
    }

    var selectedCategory by remember(availableCategories) {
        mutableStateOf(availableCategories.firstOrNull() ?: "Miscellaneous")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("add_transaction_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (type == "EXPENSE") "Add Daily Expense" else "Add Daily Income / Wages",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Type Toggle (Expense vs Income)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isExp = type == "EXPENSE"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isExp) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            type = "EXPENSE"
                            selectedCategory = "Groceries"
                        }
                        .testTag("toggle_expense_button")
                ) {
                    Text(
                        text = "💸 Expense (Spent)",
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = if (isExp) FontWeight.Bold else FontWeight.Medium,
                        color = if (isExp) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val isInc = type == "INCOME"
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isInc) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            type = "INCOME"
                            selectedCategory = "Goli Soda Business"
                        }
                        .testTag("toggle_income_button")
                ) {
                    Text(
                        text = "💰 Income / Wages",
                        modifier = Modifier.padding(vertical = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = if (isInc) FontWeight.Bold else FontWeight.Medium,
                        color = if (isInc) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (₹)") },
                prefix = { Text("₹ ", fontWeight = FontWeight.Bold) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_amount_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Title / Description
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title / Purpose (e.g. Vegetables, EB Bill, Wages)") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("transaction_title_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Category:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                OutlinedButton(
                    onClick = onOpenAddCategory,
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Category", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableCategories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Optional Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Optional Notes") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = false,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Save Button
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt != null && amt > 0 && title.isNotBlank()) {
                        onSave(type, selectedCategory, title.trim(), amt, note.trim())
                        onDismiss()
                    }
                },
                enabled = amountText.toDoubleOrNull() != null && title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_transaction_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Save Entry",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSaveCategory: (name: String, isIncome: Boolean) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Custom Category") },
        text = {
            Column {
                Text(
                    text = "Add custom expense or income categories to track your household budget:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (!isIncome) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIncome = false }
                    ) {
                        Text(
                            text = "Expense",
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isIncome) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isIncome = true }
                    ) {
                        Text(
                            text = "Income",
                            modifier = Modifier.padding(8.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name (e.g. College CSE, Milk Bill)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onSaveCategory(categoryName.trim(), isIncome)
                        onDismiss()
                    }
                },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
