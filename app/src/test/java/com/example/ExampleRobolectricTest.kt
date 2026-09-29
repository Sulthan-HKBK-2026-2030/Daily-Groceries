package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.entity.ExpenseIncomeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("DailyGrocer", appName)
  }

  @Test
  fun `test expense filtering by categories`() {
    val sampleExpenses = listOf(
      ExpenseIncomeEntity(id = 1, type = "EXPENSE", category = "Groceries", title = "Tomatoes", amount = 40.0),
      ExpenseIncomeEntity(id = 2, type = "EXPENSE", category = "Utilities", title = "Electricity Bill", amount = 450.0),
      ExpenseIncomeEntity(id = 3, type = "EXPENSE", category = "Wages", title = "Helper Wage", amount = 350.0),
      ExpenseIncomeEntity(id = 4, type = "EXPENSE", category = "Goli Soda Business", title = "Ice Block", amount = 50.0)
    )

    // Test filter Groceries
    val groceryItems = sampleExpenses.filter { it.category.equals("Groceries", ignoreCase = true) }
    assertEquals(1, groceryItems.size)
    assertEquals("Tomatoes", groceryItems[0].title)

    // Test filter Utilities
    val utilityItems = sampleExpenses.filter { it.category.equals("Utilities", ignoreCase = true) }
    assertEquals(1, utilityItems.size)
    assertEquals("Electricity Bill", utilityItems[0].title)

    // Test filter Wages
    val wageItems = sampleExpenses.filter { it.category.equals("Wages", ignoreCase = true) }
    assertEquals(1, wageItems.size)
    assertEquals("Helper Wage", wageItems[0].title)

    // Test multi-category toggle visibility: Groceries and Utilities
    val selectedCats = setOf("Groceries", "Utilities")
    val multiFiltered = sampleExpenses.filter { selectedCats.contains(it.category) }
    assertEquals(2, multiFiltered.size)
    assertTrue(multiFiltered.any { it.title == "Tomatoes" })
    assertTrue(multiFiltered.any { it.title == "Electricity Bill" })
  }
}
