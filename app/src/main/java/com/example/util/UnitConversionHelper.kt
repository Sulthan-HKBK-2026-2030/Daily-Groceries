package com.example.util

import java.util.Locale

data class WeightPreset(
    val grams: Double,
    val displayLabel: String,
    val tamilLabel: String
)

object UnitConversionHelper {

    val COMMON_PRESETS = listOf(
        WeightPreset(100.0, "100 g", "100 கிராம்"),
        WeightPreset(250.0, "250 g (1/4 kg)", "கால் கிலோ"),
        WeightPreset(500.0, "500 g (1/2 kg)", "அரை கிலோ"),
        WeightPreset(750.0, "750 g (3/4 kg)", "முக்கால் கிலோ"),
        WeightPreset(1000.0, "1 kg", "1 கிலோ"),
        WeightPreset(2000.0, "2 kg", "2 கிலோ"),
        WeightPreset(5000.0, "5 kg", "5 கிலோ")
    )

    fun gramsToKg(grams: Double): Double {
        return grams / 1000.0
    }

    fun kgToGrams(kg: Double): Double {
        return kg * 1000.0
    }

    fun calculateCost(grams: Double, pricePerKg: Double): Double {
        if (grams <= 0 || pricePerKg <= 0) return 0.0
        return (grams / 1000.0) * pricePerKg
    }

    fun formatWeightDisplay(grams: Double): String {
        return if (grams < 1000.0) {
            if (grams % 1.0 == 0.0) "${grams.toInt()} g" else String.format(Locale.getDefault(), "%.1f g", grams)
        } else {
            val kg = grams / 1000.0
            if (kg % 1.0 == 0.0) "${kg.toInt()} kg" else String.format(Locale.getDefault(), "%.2f kg", kg)
        }
    }

    fun formatConsistentInventoryUnit(quantityInput: Double, isGrams: Boolean): Pair<Double, String> {
        // Normalizes to standard Kg with 3 decimals or grams
        return if (isGrams) {
            Pair(quantityInput / 1000.0, "kg")
        } else {
            Pair(quantityInput, "kg")
        }
    }
}
