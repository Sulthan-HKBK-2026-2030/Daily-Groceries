package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class IconVisualSpec(
    val emoji: String,
    val bgStart: Color,
    val bgEnd: Color,
    val borderColor: Color
)

object IconVisualRegistry {
    fun getSpec(iconKey: String, category: String): IconVisualSpec {
        return when (iconKey.lowercase()) {
            // 🥬 Vegetables
            "onion" -> IconVisualSpec("🧅", Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFBA68C8))
            "tomato" -> IconVisualSpec("🍅", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFE57373))
            "potato" -> IconVisualSpec("🥔", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFFA1887F))
            "garlic" -> IconVisualSpec("🧄", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF176))
            "ginger" -> IconVisualSpec("🫚", Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFB74D))
            "green_chilli" -> IconVisualSpec("🌶️", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF81C784))
            "carrot" -> IconVisualSpec("🥕", Color(0xFFFBE9E7), Color(0xFFFFCCBC), Color(0xFFFF8A65))
            "beans" -> IconVisualSpec("🫘", Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF4CAF50))
            "curry_leaves" -> IconVisualSpec("🌿", Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFF4DB6AC))
            "coriander" -> IconVisualSpec("🌿", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF66BB6A))
            "mint" -> IconVisualSpec("🌱", Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF4DD0E1))
            "bottle_gourd" -> IconVisualSpec("🥒", Color(0xFFF1F8E9), Color(0xFFDCEDC8), Color(0xFFAED581))
            "brinjal" -> IconVisualSpec("🍆", Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFF9575CD))
            "bhindi" -> IconVisualSpec("🥒", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF4CAF50))
            "spinach" -> IconVisualSpec("🥬", Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF388E3C))
            "bitter_gourd" -> IconVisualSpec("🥒", Color(0xFFDCEDC8), Color(0xFFC5E1A5), Color(0xFF689F38))

            // 🍎 Fruits
            "apple" -> IconVisualSpec("🍎", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFEF5350))
            "banana" -> IconVisualSpec("🍌", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFEE58))
            "grape" -> IconVisualSpec("🍇", Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFFAB47BC))
            "orange" -> IconVisualSpec("🍊", Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFFA726))
            "lemon" -> IconVisualSpec("🍋", Color(0xFFFFFDE7), Color(0xFFFFF59D), Color(0xFFFDD835))
            "mango" -> IconVisualSpec("🥭", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFCA28))
            "pomegranate" -> IconVisualSpec("🫐", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFE53935))
            "papaya" -> IconVisualSpec("🍈", Color(0xFFFFF3E0), Color(0xFFFFCCBC), Color(0xFFFF7043))
            "kiwi" -> IconVisualSpec("🥝", Color(0xFFF1F8E9), Color(0xFFDCEDC8), Color(0xFF8BC34A))

            // 🌾 Spices
            "turmeric" -> IconVisualSpec("🟡", Color(0xFFFFF9C4), Color(0xFFFFF59D), Color(0xFFFBC02D))
            "chili_powder" -> IconVisualSpec("🌶️", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFD32F2F))
            "coriander_powder" -> IconVisualSpec("🧂", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF8D6E63))
            "jeera" -> IconVisualSpec("🌱", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF6D4C41))
            "mustard_seeds" -> IconVisualSpec("⚫", Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF546E7A))
            "garam_masala" -> IconVisualSpec("🥣", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF795548))
            "hing" -> IconVisualSpec("🧂", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFBC02D))
            "cardamom" -> IconVisualSpec("🟢", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF4CAF50))
            "cloves" -> IconVisualSpec("🤎", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF5D4037))
            "fenugreek" -> IconVisualSpec("🟡", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFA000))

            // 🌾 Pulses
            "toor_dal" -> IconVisualSpec("🟡", Color(0xFFFFF9C4), Color(0xFFFFE082), Color(0xFFFFB300))
            "urad_dal" -> IconVisualSpec("⚪", Color(0xFFFAFAFA), Color(0xFFEEEEEE), Color(0xFFBDBDBD))
            "moong_dal" -> IconVisualSpec("🟢", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF81C784))
            "chana_dal" -> IconVisualSpec("🟡", Color(0xFFFFFDE7), Color(0xFFFFF59D), Color(0xFFFDD835))
            "masoor_dal" -> IconVisualSpec("🟠", Color(0xFFFBE9E7), Color(0xFFFFCCBC), Color(0xFFFF8A65))
            "rajma" -> IconVisualSpec("🫘", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFC62828))
            "kabuli_chana" -> IconVisualSpec("🫘", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFB74D))

            // 🌾 Flours & Grains
            "atta" -> IconVisualSpec("🌾", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFA1887F))
            "rice", "basmati", "rice_flour" -> IconVisualSpec("🍚", Color(0xFFFAFAFA), Color(0xFFF5F5F5), Color(0xFFB0BEC5))
            "besan" -> IconVisualSpec("🟡", Color(0xFFFFFDE7), Color(0xFFFFF59D), Color(0xFFFBC02D))
            "poha" -> IconVisualSpec("🥣", Color(0xFFFFF8E1), Color(0xFFFFF3E0), Color(0xFFFFB74D))
            "rava" -> IconVisualSpec("🌾", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFE0E0E0))
            "vermicelli" -> IconVisualSpec("🍜", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFCA28))

            // 🌾 Oils & Ghee
            "oil", "groundnut_oil", "mustard_oil" -> IconVisualSpec("🫗", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFA000))
            "coconut_oil" -> IconVisualSpec("🥥", Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFF00897B))
            "ghee" -> IconVisualSpec("🧈", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFB300))

            // 🌾 Staples
            "sugar" -> IconVisualSpec("🍬", Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF4FC3F7))
            "salt", "crystal_salt" -> IconVisualSpec("🧂", Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE))
            "jaggery" -> IconVisualSpec("🍯", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF8D6E63))
            "tamarind" -> IconVisualSpec("🌰", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF6D4C41))
            "coconut" -> IconVisualSpec("🥥", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF795548))
            "bay_leaves" -> IconVisualSpec("🍃", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF4CAF50))

            // 🌾 Beverages
            "tea" -> IconVisualSpec("🍵", Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFD84315))
            "coffee" -> IconVisualSpec("☕", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF4E342E))
            "chaas" -> IconVisualSpec("🥛", Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF00ACC1))

            // 🌾 Snacks
            "namkeen" -> IconVisualSpec("🥨", Color(0xFFFFF3E0), Color(0xFFFFE0B2), Color(0xFFFB8C00))
            "papad" -> IconVisualSpec("🍘", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFBC02D))
            "ladoo" -> IconVisualSpec("🟡", Color(0xFFFFF8E1), Color(0xFFFFE082), Color(0xFFFFA000))
            "gulab_jamun" -> IconVisualSpec("🟤", Color(0xFFEFEBE9), Color(0xFFD7CCC8), Color(0xFF5D4037))

            // 🧼 Dairy
            "milk" -> IconVisualSpec("🥛", Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF64B5F6))
            "curd" -> IconVisualSpec("🥣", Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF29B6F6))
            "paneer" -> IconVisualSpec("🧀", Color(0xFFFFFDE7), Color(0xFFFFF9C4), Color(0xFFFFF176))
            "butter" -> IconVisualSpec("🧈", Color(0xFFFFF9C4), Color(0xFFFFF176), Color(0xFFFBC02D))

            // 🧼 Household & Care
            "dishwash" -> IconVisualSpec("🫧", Color(0xFFE0F2F1), Color(0xFF80CBC4), Color(0xFF00897B))
            "detergent" -> IconVisualSpec("🧼", Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF0288D1))
            "floor_cleaner" -> IconVisualSpec("🧴", Color(0xFFEDE7F6), Color(0xFFD1C4E9), Color(0xFF7E57C2))
            "toilet_cleaner" -> IconVisualSpec("🧴", Color(0xFFE8EAF6), Color(0xFFC5CAE9), Color(0xFF3949AB))
            "handwash" -> IconVisualSpec("🧴", Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFF009688))
            "soap" -> IconVisualSpec("🧼", Color(0xFFFCE4EC), Color(0xFFF8BBD0), Color(0xFFEC407A))
            "toothpaste" -> IconVisualSpec("🪥", Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF00ACC1))
            "shampoo" -> IconVisualSpec("🧴", Color(0xFFF3E5F5), Color(0xFFE1BEE7), Color(0xFF8E24AA))

            // 🍾 Goli Soda Business & Daily Operations
            "soda", "goli_soda", "soda_income" -> IconVisualSpec("🍾", Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00838F))
            "wages", "salary", "wage" -> IconVisualSpec("💰", Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF2E7D32))
            "bills", "utilities", "electricity", "gas", "power" -> IconVisualSpec("💡", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFA000))
            "transport" -> IconVisualSpec("🛵", Color(0xFFE3F2FD), Color(0xFF90CAF9), Color(0xFF1976D2))
            "entertainment" -> IconVisualSpec("🎬", Color(0xFFF3E5F5), Color(0xFFCE93D8), Color(0xFF7B1FA2))
            "personal_care" -> IconVisualSpec("✨", Color(0xFFFCE4EC), Color(0xFFF48FB1), Color(0xFFC2185B))
            "misc" -> IconVisualSpec("📦", Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF607D8B))
            "groceries" -> IconVisualSpec("🥬", Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF388E3C))

            else -> {
                when {
                    category.contains("Utilit", ignoreCase = true) || category.contains("Bill", ignoreCase = true) ->
                        IconVisualSpec("💡", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFA000))
                    category.contains("Wage", ignoreCase = true) || category.contains("Salary", ignoreCase = true) || category.contains("Labor", ignoreCase = true) ->
                        IconVisualSpec("💰", Color(0xFFE8F5E9), Color(0xFFA5D6A7), Color(0xFF2E7D32))
                    category.contains("Vegetables", ignoreCase = true) || category.contains("Grocer", ignoreCase = true) ->
                        IconVisualSpec("🥬", Color(0xFFE8F5E9), Color(0xFFC8E6C9), Color(0xFF81C784))
                    category.contains("Fruit", ignoreCase = true) ->
                        IconVisualSpec("🍎", Color(0xFFFFEBEE), Color(0xFFFFCDD2), Color(0xFFE57373))
                    category.contains("Dairy", ignoreCase = true) ->
                        IconVisualSpec("🥛", Color(0xFFE3F2FD), Color(0xFFBBDEFB), Color(0xFF64B5F6))
                    category.contains("Oil", ignoreCase = true) ->
                        IconVisualSpec("🫗", Color(0xFFFFF8E1), Color(0xFFFFECB3), Color(0xFFFFA000))
                    category.contains("Soda", ignoreCase = true) ->
                        IconVisualSpec("🍾", Color(0xFFE0F7FA), Color(0xFF80DEEA), Color(0xFF00838F))
                    else ->
                        IconVisualSpec("🛍️", Color(0xFFE0F2F1), Color(0xFFB2DFDB), Color(0xFF80CBC4))
                }
            }
        }
    }
}

@Composable
fun ItemIconBadge(
    iconKey: String,
    category: String = "Groceries",
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(14.dp)
) {
    val spec = IconVisualRegistry.getSpec(iconKey, category)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(spec.bgStart, spec.bgEnd)
                )
            )
            .border(1.dp, spec.borderColor.copy(alpha = 0.6f), shape),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = (size.value * 0.52f).sp
        Text(
            text = spec.emoji,
            fontSize = fontSize
        )
    }
}
