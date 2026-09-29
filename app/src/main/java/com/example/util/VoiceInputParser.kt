package com.example.util

import java.util.Locale

data class ParsedExpenseSpeech(
    val title: String,
    val amount: Double?,
    val inferredCategory: String?,
    val originalText: String
)

object VoiceInputParser {

    private val numberWords = mapOf(
        "one" to 1.0, "two" to 2.0, "three" to 3.0, "four" to 4.0, "five" to 5.0,
        "six" to 6.0, "seven" to 7.0, "eight" to 8.0, "nine" to 9.0, "ten" to 10.0,
        "fifteen" to 15.0, "twenty" to 20.0, "thirty" to 30.0, "forty" to 40.0,
        "fifty" to 50.0, "sixty" to 60.0, "seventy" to 70.0, "eighty" to 80.0,
        "ninety" to 90.0, "hundred" to 100.0, "five hundred" to 500.0, "thousand" to 1000.0
    )

    fun parseSpeech(spokenText: String, availableCategories: List<String>): ParsedExpenseSpeech {
        val clean = spokenText.trim()
        val lower = clean.lowercase(Locale.getDefault())

        // 1. Extract numeric amount
        var extractedAmount: Double? = null
        val numberRegex = Regex("""(?i)(?:₹|rs\.?|inr)?\s*(\d+(?:\.\d+)?)\s*(?:rupees|rs\.?|inr|bucks)?""")
        val match = numberRegex.find(lower)

        if (match != null && match.groupValues[1].isNotEmpty()) {
            extractedAmount = match.groupValues[1].toDoubleOrNull()
        }

        if (extractedAmount == null) {
            // Check for words
            for ((word, value) in numberWords) {
                if (lower.contains("\\b$word\\b".toRegex())) {
                    extractedAmount = value
                    break
                }
            }
        }

        // 2. Remove matched amount and filler words to extract title
        var titleCandidate = lower
        if (extractedAmount != null) {
            titleCandidate = titleCandidate.replace(numberRegex, " ")
            for (word in numberWords.keys) {
                titleCandidate = titleCandidate.replace("\\b$word\\b".toRegex(), " ")
            }
        }

        val fillerWords = listOf(
            "rupees", "rupee", "rs", "inr", "bought", "buy", "purchased",
            "spent", "cost", "for", "please", "add", "record", "today", "paid"
        )
        for (filler in fillerWords) {
            titleCandidate = titleCandidate.replace("\\b$filler\\b".toRegex(), " ")
        }

        val cleanedTitle = titleCandidate
            .replace(Regex("""[^a-zA-Z0-9\s]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

        // 3. Infer Category
        val inferredCat = when {
            lower.contains("soda") || lower.contains("goli") || lower.contains("crate") || lower.contains("bottle") -> {
                availableCategories.find { it.contains("Soda", ignoreCase = true) } ?: "Goli Soda Business"
            }
            lower.contains("tomato") || lower.contains("onion") || lower.contains("potato") ||
            lower.contains("garlic") || lower.contains("ginger") || lower.contains("vegetable") ||
            lower.contains("fruit") || lower.contains("apple") || lower.contains("banana") ||
            lower.contains("milk") || lower.contains("curd") || lower.contains("rice") ||
            lower.contains("atta") || lower.contains("sugar") || lower.contains("salt") ||
            lower.contains("oil") || lower.contains("ghee") || lower.contains("dal") ||
            lower.contains("paneer") || lower.contains("grocery") -> {
                availableCategories.find { it.contains("Grocer", ignoreCase = true) } ?: "Groceries"
            }
            lower.contains("petrol") || lower.contains("diesel") || lower.contains("auto") ||
            lower.contains("bus") || lower.contains("train") || lower.contains("cab") ||
            lower.contains("fare") || lower.contains("fuel") || lower.contains("uber") ||
            lower.contains("ola") -> {
                availableCategories.find { it.contains("Transport", ignoreCase = true) } ?: "Transportation"
            }
            lower.contains("current") || lower.contains("electricity") || lower.contains("eb") ||
            lower.contains("rent") || lower.contains("bill") || lower.contains("water") ||
            lower.contains("recharge") || lower.contains("wifi") || lower.contains("gas") -> {
                availableCategories.find { it.contains("Bill", ignoreCase = true) } ?: "Household Bills"
            }
            lower.contains("movie") || lower.contains("game") || lower.contains("cinema") ||
            lower.contains("snack") || lower.contains("party") -> {
                availableCategories.find { it.contains("Entertainment", ignoreCase = true) } ?: "Entertainment"
            }
            lower.contains("soap") || lower.contains("shampoo") || lower.contains("paste") ||
            lower.contains("medicine") || lower.contains("haircut") || lower.contains("salon") -> {
                availableCategories.find { it.contains("Personal", ignoreCase = true) } ?: "Personal Care"
            }
            else -> null
        }

        return ParsedExpenseSpeech(
            title = if (cleanedTitle.isBlank()) clean else cleanedTitle,
            amount = extractedAmount,
            inferredCategory = inferredCat,
            originalText = clean
        )
    }
}
