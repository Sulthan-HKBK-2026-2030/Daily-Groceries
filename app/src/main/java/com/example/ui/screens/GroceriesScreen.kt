package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.CartItemEntity
import com.example.data.local.entity.GroceryItemEntity
import com.example.ui.components.AddCustomGroceryItemDialog
import com.example.ui.components.CartBottomSheet
import com.example.ui.components.ItemIconBadge
import com.example.ui.components.QuickPriceCalculatorSheet
import com.example.ui.viewmodel.GroceryViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GroceriesScreen(
    groceryViewModel: GroceryViewModel,
    onDirectExpenseAdd: (title: String, amount: Double, note: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items by groceryViewModel.filteredItems.collectAsStateWithLifecycle()
    val cartItems by groceryViewModel.cartItems.collectAsStateWithLifecycle()
    val selectedCategory by groceryViewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by groceryViewModel.searchQuery.collectAsStateWithLifecycle()
    val calculatorItem by groceryViewModel.calculatorSelectedItem.collectAsStateWithLifecycle()
    val isCartOpen by groceryViewModel.isCartOpen.collectAsStateWithLifecycle()

    var showAddItemDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val totalCartPrice = cartItems.sumOf { it.calculatedPrice }

    // Group items into organized sections
    val groupedSections = remember(items) {
        items.groupBy { it.category }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Add Custom Item FAB
                FloatingActionButton(
                    onClick = { showAddItemDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.testTag("add_custom_item_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
                }

                // Shopping Basket FAB
                FloatingActionButton(
                    onClick = { groceryViewModel.openCart() },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.testTag("shopping_cart_fab")
                ) {
                    BadgedBox(
                        badge = {
                            if (cartItems.isNotEmpty()) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(text = cartItems.size.toString())
                                }
                            }
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingBag, contentDescription = "Cart")
                            if (cartItems.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "₹${totalCartPrice.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // App Banner / Top Bar with Searchable List Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🛒 Grocery & Produce Inventory",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (searchQuery.isBlank())
                                    "${items.size} items across ${groupedSections.size} organized sections"
                                else
                                    "Found ${items.size} matching items for \"$searchQuery\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Real-Time Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { groceryViewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search by name (e.g., Onion, Tomato, Sugar, Oil)...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { groceryViewModel.updateSearchQuery("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("grocery_search_input")
                    )
                }
            }

            // Categories Filter Chips Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(groceryViewModel.allCategories) { category ->
                    val isSelected = selectedCategory == category
                    val categoryEmoji = when (category) {
                        "Vegetables" -> "🥬 "
                        "Fruits" -> "🍎 "
                        "Spices & Condiments" -> "🌾 "
                        "Lentils & Pulses" -> "🫘 "
                        "Flours & Grains" -> "🌾 "
                        "Oils & Ghee" -> "🫗 "
                        "Staples & Condiments" -> "🧂 "
                        "Beverages" -> "☕ "
                        "Snacks & Sweets" -> "🥨 "
                        "Dairy Products" -> "🥛 "
                        "Household & Care" -> "🧼 "
                        else -> "📦 "
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .clickable { groceryViewModel.selectCategory(category) }
                            .testTag("category_chip_$category")
                    ) {
                        Text(
                            text = if (category == "All Items") "All Items" else "$categoryEmoji$category",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Search Results Summary Banner when filtering
            if (searchQuery.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Filter: \"$searchQuery\" (${items.size} items in ${groupedSections.size} sections)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Clear filter",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { groceryViewModel.updateSearchQuery("") }
                        )
                    }
                }
            }

            // Items List Organized by Sections
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No items found for \"$searchQuery\"",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching by local name, or tap + to add a custom item",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Iterate through each organized section
                    groupedSections.forEach { (sectionCategory, sectionItems) ->
                        // Section Header
                        item(key = "header_$sectionCategory") {
                            SectionHeader(
                                category = sectionCategory,
                                itemCount = sectionItems.size,
                                searchActive = searchQuery.isNotBlank()
                            )
                        }

                        // Section Items
                        items(sectionItems, key = { it.id }) { item ->
                            GroceryItemCard(
                                item = item,
                                onCardClick = { groceryViewModel.openCalculator(item) },
                                onQuickAdd = { qtyGramsOrUnits, label, price ->
                                    val cartItem = CartItemEntity(
                                        groceryItemId = item.id,
                                        name = item.name,
                                        nativeName = item.nativeName,
                                        category = item.category,
                                        iconKey = item.iconKey,
                                        unitPrice = item.pricePerUnit,
                                        unit = item.unit,
                                        selectedQuantityValue = qtyGramsOrUnits,
                                        quantityLabel = label,
                                        calculatedPrice = price
                                    )
                                    groceryViewModel.addToCart(cartItem)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Added $label ${item.name} (₹${price.toInt()})")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Quick Weight & Price Calculator
    calculatorItem?.let { item ->
        QuickPriceCalculatorSheet(
            item = item,
            onDismiss = { groceryViewModel.closeCalculator() },
            onAddToCart = { cartItem ->
                groceryViewModel.addToCart(cartItem)
                scope.launch {
                    snackbarHostState.showSnackbar("Added ${cartItem.quantityLabel} ${cartItem.name} to Shopping Bag")
                }
            },
            onDirectExpenseAdd = { title, amount, note ->
                onDirectExpenseAdd(title, amount, note)
                scope.launch {
                    snackbarHostState.showSnackbar("Logged ₹${amount.toInt()} in Daily Expenses")
                }
            },
            onUpdateDailyPrice = { newPrice ->
                groceryViewModel.updateItemPrice(item.id, newPrice)
                scope.launch {
                    snackbarHostState.showSnackbar("Updated ${item.name} price to ₹$newPrice/${item.unit}")
                }
            }
        )
    }

    // Modal Bottom Sheet: Shopping Cart & Bill
    if (isCartOpen) {
        CartBottomSheet(
            cartItems = cartItems,
            onDismiss = { groceryViewModel.closeCart() },
            onRemoveItem = { id -> groceryViewModel.removeCartItem(id) },
            onClearCart = { groceryViewModel.clearCart() },
            onLogToDailyExpense = { totalAmount, summaryNote ->
                onDirectExpenseAdd(
                    "Daily Groceries & Vegetables Bill (${cartItems.size} items)",
                    totalAmount,
                    summaryNote
                )
                scope.launch {
                    snackbarHostState.showSnackbar("Saved ₹${totalAmount.toInt()} grocery bill to Daily Expenses!")
                }
            }
        )
    }

    // Add Custom Grocery Item Dialog
    if (showAddItemDialog) {
        AddCustomGroceryItemDialog(
            categories = groceryViewModel.allCategories,
            onDismiss = { showAddItemDialog = false },
            onSaveItem = { name, nativeName, category, price, unit ->
                groceryViewModel.addNewItem(name, nativeName, category, price, unit)
                scope.launch {
                    snackbarHostState.showSnackbar("Added $name at ₹$price/$unit")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(
    category: String,
    itemCount: Int,
    searchActive: Boolean
) {
    val (icon, desc) = when (category) {
        "Vegetables" -> "🥬" to "Fresh produce to stock up weekly for everyday cooking"
        "Fruits" -> "🍎" to "Healthy everyday fruits for quick snacking or breakfast"
        "Spices & Condiments" -> "🌾" to "Spices, powders, seeds & seasoning essentials"
        "Lentils & Pulses" -> "🫘" to "Dals & pulses rich in protein for curries & gravies"
        "Flours & Grains" -> "🌾" to "Rice, atta, sooji & pantry grain staples"
        "Oils & Ghee" -> "🫗" to "Cooking oils & pure aromatic ghee"
        "Staples & Condiments" -> "🧂" to "Salt, sugar, jaggery, tamarind & coconut"
        "Beverages" -> "☕" to "Tea dust, filter coffee & refreshing buttermilk"
        "Snacks & Sweets" -> "🥨" to "Savouries, papads & traditional sweets"
        "Dairy Products" -> "🥛" to "Fresh milk, curd, paneer & butter"
        "Household & Care" -> "🧼" to "Cleaning supplies & personal care items"
        else -> "📦" to "Household essentials"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = icon, fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Text(
                    text = if (searchActive) "$itemCount matches" else "$itemCount items",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(start = 28.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroceryItemCard(
    item: GroceryItemEntity,
    onCardClick: () -> Unit,
    onQuickAdd: (Double, String, Double) -> Unit
) {
    val isWeight = item.unit.lowercase() == "kg"
    val isVolume = item.unit.lowercase() == "l"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("grocery_item_card_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Main item details row with visual icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemIconBadge(
                    iconKey = item.iconKey,
                    category = item.category,
                    size = 50.dp
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.nativeName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Daily Rate Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "₹${String.format(Locale.getDefault(), "%.0f", item.pricePerUnit)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "per ${item.unit}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick weight calculation chips row (Sulthan's core feature!)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Quick 1-Tap Buy:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Tap card for custom grams",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isWeight) {
                    val presets = listOf(
                        100.0 to "100g",
                        250.0 to "250g (¼)",
                        500.0 to "500g (½)",
                        750.0 to "750g (¾)",
                        1000.0 to "1kg"
                    )
                    presets.forEach { (grams, label) ->
                        val price = (grams / 1000.0) * item.pricePerUnit
                        QuickAddChip(
                            label = label,
                            priceText = "₹${String.format(Locale.getDefault(), "%.1f", price)}",
                            onClick = {
                                onQuickAdd(grams, "$label", price)
                            }
                        )
                    }
                } else if (isVolume) {
                    val presets = listOf(
                        250.0 to "250ml",
                        500.0 to "500ml",
                        1000.0 to "1 Litre"
                    )
                    presets.forEach { (ml, label) ->
                        val price = (ml / 1000.0) * item.pricePerUnit
                        QuickAddChip(
                            label = label,
                            priceText = "₹${String.format(Locale.getDefault(), "%.1f", price)}",
                            onClick = {
                                onQuickAdd(ml, label, price)
                            }
                        )
                    }
                } else {
                    val presets = listOf(1.0, 2.0, 5.0)
                    presets.forEach { units ->
                        val price = units * item.pricePerUnit
                        QuickAddChip(
                            label = "${units.toInt()} ${item.unit}",
                            priceText = "₹${String.format(Locale.getDefault(), "%.0f", price)}",
                            onClick = {
                                onQuickAdd(units, "${units.toInt()} ${item.unit}", price)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuickAddChip(
    label: String,
    priceText: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = priceText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
