package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.SodaSaleEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExportHelper {

    fun generateTransactionsCsv(transactions: List<ExpenseIncomeEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val sb = StringBuilder()
        sb.append("Date,Time,Type,Category,Title,Amount (INR),Notes\n")

        for (t in transactions) {
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val timeStr = timeFormat.format(Date(t.dateMillis))
            val safeCategory = escapeCsv(t.category)
            val safeTitle = escapeCsv(t.title)
            val safeNote = escapeCsv(t.note)
            val amountFormatted = String.format(Locale.getDefault(), "%.2f", t.amount)

            sb.append("$dateStr,$timeStr,${t.type},$safeCategory,$safeTitle,$amountFormatted,$safeNote\n")
        }

        return sb.toString()
    }

    fun generateSodaSalesCsv(sales: List<SodaSaleEntity>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val sb = StringBuilder()
        sb.append("Date,Time,Flavor,Bottles Sold,Price Per Bottle (INR),Total Revenue (INR),Notes\n")

        for (s in sales) {
            val dateStr = dateFormat.format(Date(s.dateMillis))
            val timeStr = timeFormat.format(Date(s.dateMillis))
            val safeFlavor = escapeCsv(s.flavor)
            val safeNote = escapeCsv(s.notes)
            val rateFormatted = String.format(Locale.getDefault(), "%.2f", s.pricePerBottle)
            val totalFormatted = String.format(Locale.getDefault(), "%.2f", s.totalRevenue)

            sb.append("$dateStr,$timeStr,$safeFlavor,${s.bottlesSold},$rateFormatted,$totalFormatted,$safeNote\n")
        }

        return sb.toString()
    }

    fun generateCombinedFinancialReportCsv(
        transactions: List<ExpenseIncomeEntity>,
        sodaSales: List<SodaSaleEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

        val totalIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpense
        val totalSodaBottles = sodaSales.sumOf { it.bottlesSold }
        val totalSodaRevenue = sodaSales.sumOf { it.totalRevenue }

        val sb = StringBuilder()
        sb.append("# DailyGrocer Financial & Goli Soda Report\n")
        sb.append("# Generated on: ${dateFormat.format(Date())} ${timeFormat.format(Date())}\n")
        sb.append("# Summary: Total Income=INR $totalIncome, Total Expenses=INR $totalExpense, Net Balance=INR $netBalance\n")
        sb.append("# Goli Soda Business: $totalSodaBottles bottles sold, Total Soda Revenue=INR $totalSodaRevenue\n\n")

        sb.append("=== CASHBOOK TRANSACTIONS ===\n")
        sb.append("Date,Time,Type,Category,Title,Amount (INR),Notes\n")
        for (t in transactions) {
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val timeStr = timeFormat.format(Date(t.dateMillis))
            sb.append("$dateStr,$timeStr,${t.type},${escapeCsv(t.category)},${escapeCsv(t.title)},${String.format(Locale.getDefault(), "%.2f", t.amount)},${escapeCsv(t.note)}\n")
        }

        sb.append("\n=== GOLI SODA SALES LOG ===\n")
        sb.append("Date,Time,Flavor,Bottles Sold,Rate (INR),Total Revenue (INR),Notes\n")
        for (s in sodaSales) {
            val dateStr = dateFormat.format(Date(s.dateMillis))
            val timeStr = timeFormat.format(Date(s.dateMillis))
            sb.append("$dateStr,$timeStr,${escapeCsv(s.flavor)},${s.bottlesSold},${String.format(Locale.getDefault(), "%.2f", s.pricePerBottle)},${String.format(Locale.getDefault(), "%.2f", s.totalRevenue)},${escapeCsv(s.notes)}\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\r", " ").replace("\n", " ")
        return if (str.contains(",") || str.contains("\"") || str.contains(";")) {
            "\"" + str.replace("\"", "\"\"") + "\""
        } else {
            str
        }
    }

    fun shareCsvContent(context: Context, csvData: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, csvData)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/csv"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export / Share CSV")
        context.startActivity(shareIntent)
    }

    fun copyToClipboard(context: Context, text: String, label: String = "CSV Export") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
