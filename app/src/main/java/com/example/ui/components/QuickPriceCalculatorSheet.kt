package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.GroceryItemEntity
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuickPriceCalculatorSheet(
    item: GroceryItemEntity,
    onDismiss: () -> Unit,
    onAddToCart: (CartItemEntity) -> Unit,
    onDirectExpenseAdd: (title: String, amount: Double, note: String) -> Unit,
    onUpdateDailyPrice: (newPrice: Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Current price per unit state (allows inline manual override)
    var currentUnitPrice by remember(item.pricePerUnit) { mutableDoubleStateOf(item.pricePerUnit) }
    var isEditingPrice by remember { mutableStateOf(false) }
    var priceInputText by remember { mutableStateOf(item.pricePerUnit.toString()) }

    // Selected quantity in fundamental units (for kg: in grams; for L: in ml; for pc/pack: count)
    val isWeightItem = item.unit.lowercase() == "kg"
    val isVolumeItem = item.unit.lowercase() == "l"

    // Default to 1000g (1kg) or 1 unit
    var selectedGramsOrUnits by remember {
        mutableDoubleStateOf(if (isWeightItem) 500.0 else if (isVolumeItem) 1000.0 else 1.0)
    }
    var customInputText by remember {
        mutableStateOf(
            if (isWeightItem) "500" else if (isVolumeItem) "1000" else "1"
        )
    }

    // Calculate total price
    val calculatedPrice = remember(selectedGramsOrUnits, currentUnitPrice, item.unit) {
        when {
            isWeightItem -> (selectedGramsOrUnits / 1000.0) * currentUnitPrice
            isVolumeItem -> (selectedGramsOrUnits / 1000.0) * currentUnitPrice
            else -> selectedGramsOrUnits * currentUnitPrice
        }
    }

    // Format display string
    val quantityDisplayLabel = remember(selectedGramsOrUnits, item.unit) {
        when {
            isWeightItem -> {
                if (selectedGramsOrUnits >= 1000.0 && selectedGramsOrUnits % 1000.0 == 0.0) {
                    "${(selectedGramsOrUnits / 1000.0).toInt()} Kg"
                } else if (selectedGramsOrUnits >= 1000.0) {
                    String.format(Locale.getDefault(), "%.2f Kg", selectedGramsOrUnits / 1000.0)
                } else {
                    "${selectedGramsOrUnits.toInt()} Grams"
                }
            }
            isVolumeItem -> {
                if (selectedGramsOrUnits >= 1000.0 && selectedGramsOrUnits % 1000.0 == 0.0) {
                    "${(selectedGramsOrUnits / 1000.0).toInt()} Litre"
                } else if (selectedGramsOrUnits >= 1000.0) {
                    String.format(Locale.getDefault(), "%.2f Litre", selectedGramsOrUnits / 1000.0)
                } else {
                    "${selectedGramsOrUnits.toInt()} ml"
                }
            }
            else -> {
                if (selectedGramsOrUnits % 1.0 == 0.0) {
                    "${selectedGramsOrUnits.toInt()} ${item.unit}"
                } else {
                    "$selectedGramsOrUnits ${item.unit}"
                }
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        modifier = Modifier.testTag("quick_price_calculator_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row with Visual Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemIconBadge(
                    iconKey = item.iconKey,
                    category = item.category,
                    size = 56.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = item.nativeName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Market Rate Row (Manual Price setting)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Daily Market Price",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!isEditingPrice) {
                            Text(
                                text = "₹${String.format(Locale.getDefault(), "%.2f", currentUnitPrice)} / ${item.unit}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 4.dp))
                                OutlinedTextField(
                                    value = priceInputText,
                                    onValueChange = { priceInputText = it },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        val newP = priceInputText.toDoubleOrNull()
                                        if (newP != null && newP > 0) {
                                            currentUnitPrice = newP
                                            onUpdateDailyPrice(newP)
                                        }
                                        isEditingPrice = false
                                    }
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }

                    if (!isEditingPrice) {
                        OutlinedButton(
                            onClick = {
                                priceInputText = currentUnitPrice.toString()
                                isEditingPrice = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Price",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Change Rate")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Grams / Quantity Buttons (Sulthan's core feature!)
            Text(
                text = if (isWeightItem) "Select Weight (Grams / Kg):" else "Select Quantity:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isWeightItem) {
                // Preset weight chips: 100g, 250g, 500g, 750g, 1kg, 2kg
                val weightPresets = listOf(
                    100.0 to "100 Gram",
                    250.0 to "250 Gram (¼ kg)",
                    500.0 to "500 Gram (½ kg)",
                    750.0 to "750 Gram (¾ kg)",
                    1000.0 to "1 KG",
                    1500.0 to "1.5 KG",
                    2000.0 to "2 KG"
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    weightPresets.forEach { (grams, label) ->
                        val isSelected = selectedGramsOrUnits == grams
                        val chipPrice = (grams / 1000.0) * currentUnitPrice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedGramsOrUnits = grams
                                    customInputText = grams.toInt().toString()
                                }
                                .testTag("chip_weight_${grams.toInt()}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.2f", chipPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else if (isVolumeItem) {
                val volumePresets = listOf(
                    250.0 to "250 ml",
                    500.0 to "500 ml",
                    1000.0 to "1 Litre",
                    2000.0 to "2 Litres",
                    5000.0 to "5 Litres"
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    volumePresets.forEach { (ml, label) ->
                        val isSelected = selectedGramsOrUnits == ml
                        val chipPrice = (ml / 1000.0) * currentUnitPrice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable {
                                selectedGramsOrUnits = ml
                                customInputText = ml.toInt().toString()
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.2f", chipPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Unit presets: 1, 2, 3, 4, 5, 10
                val unitPresets = listOf(1.0, 2.0, 3.0, 4.0, 5.0, 10.0)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    unitPresets.forEach { units ->
                        val isSelected = selectedGramsOrUnits == units
                        val chipPrice = units * currentUnitPrice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.clickable {
                                selectedGramsOrUnits = units
                                customInputText = units.toInt().toString()
                            }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${units.toInt()} ${item.unit}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                                Text(
                                    text = "₹${String.format(Locale.getDefault(), "%.2f", chipPrice)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Grams / Quantity Fine-Tuning
            Text(
                text = if (isWeightItem) "Custom Grams (Less or More):" else "Custom Count / Amount:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val step = if (isWeightItem) 50.0 else if (isVolumeItem) 100.0 else 1.0
                        if (selectedGramsOrUnits > step) {
                            selectedGramsOrUnits -= step
                            customInputText = if (isWeightItem || isVolumeItem) selectedGramsOrUnits.toInt().toString() else selectedGramsOrUnits.toString()
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease")
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedTextField(
                    value = customInputText,
                    onValueChange = { input ->
                        customInputText = input
                        val parsed = input.toDoubleOrNull()
                        if (parsed != null && parsed >= 0) {
                            selectedGramsOrUnits = parsed
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    suffix = {
                        Text(
                            text = if (isWeightItem) "g" else if (isVolumeItem) "ml" else item.unit,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_quantity_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(12.dp))

                IconButton(
                    onClick = {
                        val step = if (isWeightItem) 50.0 else if (isVolumeItem) 100.0 else 1.0
                        selectedGramsOrUnits += step
                        customInputText = if (isWeightItem || isVolumeItem) selectedGramsOrUnits.toInt().toString() else selectedGramsOrUnits.toString()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Calculated Price Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Price to Pay",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "₹${String.format(Locale.getDefault(), "%.2f", calculatedPrice)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "For $quantityDisplayLabel of ${item.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Button(
                onClick = {
                    val cartItem = CartItemEntity(
                        groceryItemId = item.id,
                        name = item.name,
                        nativeName = item.nativeName,
                        category = item.category,
                        iconKey = item.iconKey,
                        unitPrice = currentUnitPrice,
                        unit = item.unit,
                        selectedQuantityValue = selectedGramsOrUnits,
                        quantityLabel = quantityDisplayLabel,
                        calculatedPrice = calculatedPrice
                    )
                    onAddToCart(cartItem)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_to_cart_confirm_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add to Shopping Bag",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onDirectExpenseAdd(
                        "Bought ${item.name} ($quantityDisplayLabel)",
                        calculatedPrice,
                        "Grocery direct purchase at ₹$currentUnitPrice/${item.unit}"
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("direct_add_expense_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Payments, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Pay & Record in Daily Expenses")
            }
        }
    }
}
