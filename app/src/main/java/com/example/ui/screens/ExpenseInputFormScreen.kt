package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ItemIconBadge
import com.example.util.UnitConversionHelper
import com.example.util.VoiceInputParser
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExpenseInputFormScreen(
    initialCategory: String = "Groceries",
    initialAmount: Double? = null,
    initialTitle: String = "",
    initialNote: String = "",
    availableCategories: List<String> = listOf(
        "Groceries",
        "Utilities",
        "Wages",
        "Goli Soda Business",
        "Household Bills",
        "Transportation",
        "Entertainment",
        "Personal Care",
        "Miscellaneous"
    ),
    onSaveExpense: (category: String, title: String, amount: Double, note: String, paymentMode: String, dateMillis: Long) -> Unit,
    onCancel: () -> Unit,
    onAddCustomCategory: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var amountInput by remember { mutableStateOf(initialAmount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var titleInput by remember { mutableStateOf(initialTitle) }
    var noteInput by remember { mutableStateOf(initialNote) }
    var paymentMode by remember { mutableStateOf("💵 Cash") }

    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    val selectedDateDisplay = remember(selectedDateMillis) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
        val isToday = now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val isYesterday = yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)

        when {
            isToday -> "Today (${SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))})"
            isYesterday -> "Yesterday (${SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))})"
            else -> SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
        }
    }

    var showUnitConverter by remember { mutableStateOf(initialCategory == "Groceries") }
    var weightInput by remember { mutableStateOf("500") }
    var isWeightInGrams by remember { mutableStateOf(true) }
    var pricePerKgInput by remember { mutableStateOf("40") }

    val context = LocalContext.current
    var speechFeedback by remember { mutableStateOf<String?>(null) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val topSpoken = spokenMatches?.firstOrNull()
            if (!topSpoken.isNullOrBlank()) {
                val parsed = VoiceInputParser.parseSpeech(topSpoken, availableCategories)
                if (parsed.amount != null) {
                    amountInput = if (parsed.amount % 1.0 == 0.0) parsed.amount.toInt().toString() else parsed.amount.toString()
                }
                if (parsed.title.isNotBlank()) {
                    titleInput = parsed.title
                }
                if (parsed.inferredCategory != null) {
                    selectedCategory = parsed.inferredCategory
                }
                speechFeedback = "Heard: \"$topSpoken\""
                Toast.makeText(context, "Filled: ${parsed.title} ₹${parsed.amount ?: ""}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun launchVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak expense (e.g. 'Tomato 40 rupees' or 'Milk 30')")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "Speech recognition not available", Toast.LENGTH_SHORT).show()
        }
    }

    var hasAttemptedSave by remember { mutableStateOf(false) }

    val amountDouble = amountInput.toDoubleOrNull()
    val amountError: String? = when {
        amountInput.isBlank() -> "Price / amount is required"
        amountDouble == null -> "Please enter a valid numeric value"
        amountDouble <= 0.0 -> "Price must be greater than ₹0"
        amountDouble > 10_000_000.0 -> "Price exceeds allowed limit"
        else -> null
    }
    val isAmountValid = amountError == null

    val weightDouble = weightInput.toDoubleOrNull()
    val weightError: String? = when {
        weightInput.isBlank() -> "Quantity is required"
        weightDouble == null -> "Enter valid numeric quantity"
        weightDouble <= 0.0 -> "Quantity must be > 0"
        weightDouble > 100_000.0 -> "Quantity exceeds allowed limit"
        else -> null
    }

    val pricePerKgDouble = pricePerKgInput.toDoubleOrNull()
    val pricePerKgError: String? = when {
        pricePerKgInput.isBlank() -> "Price per kg is required"
        pricePerKgDouble == null -> "Enter valid numeric price"
        pricePerKgDouble <= 0.0 -> "Price per kg must be > ₹0"
        pricePerKgDouble > 100_000.0 -> "Price exceeds allowed limit"
        else -> null
    }

    val isWeightPriceValid = weightError == null && pricePerKgError == null
    val isValid = isAmountValid

    // Contextual suggestions based on chosen category
    val suggestions = remember(selectedCategory) {
        when (selectedCategory) {
            "Groceries" -> listOf("Vegetables & Fruits", "Monthly Provisions", "Milk & Curd", "Rice & Atta", "Cooking Oil")
            "Goli Soda Business" -> listOf("Ice Blocks", "CO2 Gas Refill", "Flavors & Sugar", "Bottle Deposit", "Stall Rent")
            "Household Bills" -> listOf("Electricity EB Bill", "Gas Cylinder", "Water Charges", "Mobile Recharge", "WiFi / Internet")
            "Transportation" -> listOf("Petrol / Fuel", "Bus Ticket", "Auto / Share Auto", "Train Ticket", "Vehicle Repair")
            "Entertainment" -> listOf("Tea & Snacks", "Movie Ticket", "Family Outing", "Festival Treats")
            "Personal Care" -> listOf("Soaps & Shampoo", "Haircut / Grooming", "Medical & Pharmacy", "Toothpaste & Care")
            else -> listOf("Daily Essential", "Emergency Need", "Home Repair", "General Purchase")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Record Daily Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add to today's cashbook & budget",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier.testTag("expense_form_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { launchVoiceRecognition() },
                        modifier = Modifier.testTag("expense_form_mic_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice Input",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // 🎙️ Voice-to-Text Speech Input Banner
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { launchVoiceRecognition() }
                    .testTag("voice_input_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Microphone",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "🎙️ Quick Voice Entry",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = speechFeedback ?: "Speak: \"Tomato 40 rupees\" or \"Petrol 200\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (speechFeedback != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { launchVoiceRecognition() },
                        modifier = Modifier.testTag("voice_input_speak_button")
                    ) {
                        Text("Speak", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 📅 Date Selection Card (Allows logging for past dates)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePickerDialog = true }
                    .testTag("expense_date_picker_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = "Select Date",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "Expense Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedDateDisplay,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    FilledTonalButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier.testTag("expense_date_picker_button")
                    ) {
                        Text("Change Date", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Amount Input Card (Prominent & Clean)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Enter Amount Spent",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "₹",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )

                        OutlinedTextField(
                            value = amountInput,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                    amountInput = input
                                }
                            },
                            placeholder = {
                                Text(
                                    "0",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            isError = hasAttemptedSave && !isAmountValid,
                            supportingText = {
                                if (hasAttemptedSave && !isAmountValid && amountError != null) {
                                    Text(
                                        text = amountError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.labelSmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            trailingIcon = {
                                if (amountInput.isNotEmpty()) {
                                    IconButton(onClick = { amountInput = "" }) {
                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .width(240.dp)
                                .testTag("expense_form_amount_input")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Amount Addition Chips
                    Text(
                        text = "Quick Add:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(50, 100, 200, 500, 1000).forEach { addVal ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val current = amountInput.toDoubleOrNull() ?: 0.0
                                        amountInput = (current + addVal).toInt().toString()
                                    }
                                    .testTag("quick_amount_add_$addVal")
                            ) {
                                Text(
                                    text = "+₹$addVal",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Category Selector Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Select Expense Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (onAddCustomCategory != null) {
                    OutlinedButton(
                        onClick = onAddCustomCategory,
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Custom", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val iconKey = when (category) {
                        "Groceries" -> "groceries"
                        "Goli Soda Business", "Goli Soda" -> "soda"
                        "Household Bills", "Bills" -> "bills"
                        "Transportation" -> "transport"
                        "Entertainment" -> "entertainment"
                        "Personal Care" -> "personal_care"
                        else -> "misc"
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .clickable {
                                selectedCategory = category
                                if (titleInput.isBlank()) {
                                    titleInput = category
                                }
                            }
                            .testTag("expense_category_chip_$category")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            ItemIconBadge(
                                iconKey = iconKey,
                                category = category,
                                size = 28.dp,
                                shape = RoundedCornerShape(8.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ⚖️ Grams ⇄ Kilograms Unit Converter & Price Helper
            val weightVal = weightInput.toDoubleOrNull() ?: 0.0
            val totalGrams = if (isWeightInGrams) weightVal else weightVal * 1000.0
            val totalKg = totalGrams / 1000.0
            val pricePerKg = pricePerKgInput.toDoubleOrNull() ?: 0.0
            val calculatedCost = UnitConversionHelper.calculateCost(totalGrams, pricePerKg)

            if (selectedCategory == "Groceries" || showUnitConverter) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("grocery_unit_converter_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Scale,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Column {
                                    Text(
                                        text = "⚖️ Grams ⇄ Kg Price Helper",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Auto-calculate cost & maintain unit consistency",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Presets Row
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            UnitConversionHelper.COMMON_PRESETS.forEach { preset ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (totalGrams == preset.grams) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (totalGrams == preset.grams) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    ),
                                    modifier = Modifier.clickable {
                                        if (preset.grams < 1000.0) {
                                            isWeightInGrams = true
                                            weightInput = preset.grams.toInt().toString()
                                        } else {
                                            isWeightInGrams = false
                                            weightInput = (preset.grams / 1000.0).toInt().toString()
                                        }
                                    }
                                ) {
                                    Text(
                                        text = preset.displayLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (totalGrams == preset.grams) FontWeight.Bold else FontWeight.Normal,
                                        color = if (totalGrams == preset.grams) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        // Input fields: Weight & Rate/Kg
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Weight Input + Unit Toggle
                            Column(modifier = Modifier.weight(1.2f)) {
                                Text("Quantity / Weight", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = weightInput,
                                        onValueChange = { input ->
                                            if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,3}\$"))) {
                                                weightInput = input
                                            }
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        isError = weightInput.isNotEmpty() && weightError != null,
                                        supportingText = {
                                            if (weightInput.isNotEmpty() && weightError != null) {
                                                Text(weightError, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    OutlinedButton(
                                        onClick = { isWeightInGrams = !isWeightInGrams },
                                        modifier = Modifier.height(52.dp),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(if (isWeightInGrams) "g" else "kg", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Price per kg
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rate (₹ per kg)", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = pricePerKgInput,
                                    onValueChange = { input ->
                                        if (input.isEmpty() || input.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                                            pricePerKgInput = input
                                        }
                                    },
                                    prefix = { Text("₹") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    isError = pricePerKgInput.isNotEmpty() && pricePerKgError != null,
                                    supportingText = {
                                        if (pricePerKgInput.isNotEmpty() && pricePerKgError != null) {
                                            Text(pricePerKgError, color = MaterialTheme.colorScheme.error, fontSize = 10.sp)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Conversion Result Banner & Quick Apply Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${UnitConversionHelper.formatWeightDisplay(totalGrams)} = ${String.format(Locale.getDefault(), "%.3f", totalKg)} kg",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Total: ₹${String.format(Locale.getDefault(), "%.2f", calculatedCost)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Button(
                                    onClick = {
                                        if (!isWeightPriceValid) {
                                            val err = weightError ?: pricePerKgError ?: "Invalid quantity or rate"
                                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        amountInput = if (calculatedCost % 1.0 == 0.0) calculatedCost.toInt().toString() else String.format(Locale.getDefault(), "%.2f", calculatedCost)
                                        val weightLabel = UnitConversionHelper.formatWeightDisplay(totalGrams)
                                        if (titleInput.isBlank() || titleInput == "Groceries") {
                                            titleInput = "Vegetables ($weightLabel)"
                                        } else if (!titleInput.contains(weightLabel)) {
                                            titleInput = "$titleInput ($weightLabel)"
                                        }
                                        noteInput = "Unit consistency: $weightLabel (${String.format(Locale.getDefault(), "%.3f", totalKg)} kg) @ ₹$pricePerKgDouble/kg"
                                        Toast.makeText(context, "Applied ₹${amountInput} for $weightLabel", Toast.LENGTH_SHORT).show()
                                    },
                                    enabled = isWeightPriceValid && calculatedCost > 0
                                ) {
                                    Text("Apply ₹${if (calculatedCost % 1.0 == 0.0) calculatedCost.toInt().toString() else String.format(Locale.getDefault(), "%.1f", calculatedCost)}")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // 3. Expense Title & Suggestions
            Text(
                text = "Expense Title / Reason",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = titleInput,
                onValueChange = { titleInput = it },
                placeholder = { Text("e.g. Vegetables, EB electricity, Auto fare...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_title_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Suggestions Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                suggestions.forEach { sugg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.clickable { titleInput = sugg }
                    ) {
                        Text(
                            text = sugg,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. Optional Note
            Text(
                text = "Optional Note / Remarks",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = { Text("Add extra details (e.g. bought from market, shop receipt #, paid by brother)...") },
                maxLines = 3,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("expense_form_note_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 5. Payment Mode Selector
            Text(
                text = "Payment Mode",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("💵 Cash", "📱 UPI / GPay", "💳 Card / NetBanking").forEach { mode ->
                    val isSelected = paymentMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { paymentMode = mode }
                    ) {
                        Text(
                            text = mode,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))

            // 6. Action Buttons
            Button(
                onClick = {
                    hasAttemptedSave = true
                    if (!isAmountValid) {
                        Toast.makeText(
                            context,
                            amountError ?: "Please enter a valid numeric price / amount",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@Button
                    }
                    val validAmount = amountDouble ?: 0.0
                    if (validAmount <= 0.0) {
                        Toast.makeText(context, "Price must be greater than ₹0", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val finalTitle = if (titleInput.isNotBlank()) titleInput.trim() else selectedCategory
                    val combinedNote = if (noteInput.isNotBlank()) "${noteInput.trim()} [$paymentMode]" else "Paid via $paymentMode"
                    onSaveExpense(
                        selectedCategory,
                        finalTitle,
                        validAmount,
                        combinedNote,
                        paymentMode,
                        selectedDateMillis
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("expense_form_save_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Expense (₹${if (isAmountValid && (amountDouble ?: 0.0) > 0) String.format(Locale.getDefault(), "%.0f", amountDouble ?: 0.0) else "0"})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("expense_form_cancel_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancel / Discard")
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Material 3 DatePickerDialog to log past dates
    if (showDatePickerDialog) {
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val picked = datePickerState.selectedDateMillis
                        if (picked != null) {
                            selectedDateMillis = picked
                        }
                        showDatePickerDialog = false
                    },
                    modifier = Modifier.testTag("date_picker_confirm_button")
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDatePickerDialog = false },
                    modifier = Modifier.testTag("date_picker_cancel_button")
                ) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = true
            )
        }
    }
}
