package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpenseIncomeEntity
import com.example.data.local.entity.SodaSaleEntity
import com.example.util.DatabaseBackupManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CsvExportDialog(
    transactions: List<ExpenseIncomeEntity>,
    sodaSales: List<SodaSaleEntity>,
    onDismiss: () -> Unit,
    onCopied: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    val db = remember { AppDatabase.getDatabase(context) }
    val groceryDao = remember { db.groceryDao() }
    val cashbookDao = remember { db.cashbookDao() }
    val sodaDao = remember { db.sodaDao() }

    var jsonDumpText by remember { mutableStateOf<String?>(null) }
    var isGeneratingJson by remember { mutableStateOf(false) }

    // Dialog state for restoring from JSON
    var showRestorePrompt by remember { mutableStateOf(false) }
    var restoreInputJson by remember { mutableStateOf("") }
    var isRestoring by remember { mutableStateOf(false) }

    val expensesCsv = remember(transactions) {
        val sb = StringBuilder()
        sb.append("ID,Date,Type,Category,Title,Amount(INR),Note\n")
        transactions.forEach { t ->
            val dateStr = dateFormat.format(Date(t.dateMillis))
            val cleanTitle = t.title.replace(",", ";").replace("\"", "'")
            val cleanNote = (t.note ?: "").replace(",", ";").replace("\"", "'")
            sb.append("${t.id},\"$dateStr\",${t.type},\"${t.category}\",\"$cleanTitle\",${t.amount},\"$cleanNote\"\n")
        }
        sb.toString()
    }

    val sodaCsv = remember(sodaSales) {
        val sb = StringBuilder()
        sb.append("ID,Date,Flavor,Bottles_Sold,Price_Per_Bottle(INR),Total_Income(INR),Notes\n")
        sodaSales.forEach { s ->
            val dateStr = dateFormat.format(Date(s.dateMillis))
            val cleanNotes = s.notes.replace(",", ";").replace("\"", "'")
            sb.append("${s.id},\"$dateStr\",${s.flavor},${s.bottlesSold},${s.pricePerBottle},${s.totalRevenue},\"$cleanNotes\"\n")
        }
        sb.toString()
    }

    val combinedCsv = remember(expensesCsv, sodaCsv) {
        "=== DAILY TRANSACTIONS & EXPENSES ===\n$expensesCsv\n\n=== GOLI SODA BUSINESS SALES ===\n$sodaCsv"
    }

    // Load JSON Dump when Tab 3 is selected
    LaunchedEffect(selectedTab) {
        if (selectedTab == 3 && jsonDumpText == null) {
            isGeneratingJson = true
            jsonDumpText = DatabaseBackupManager.exportDatabaseToJson(groceryDao, cashbookDao, sodaDao)
            isGeneratingJson = false
        }
    }

    val currentContent = when (selectedTab) {
        0 -> expensesCsv
        1 -> sodaCsv
        2 -> combinedCsv
        else -> jsonDumpText ?: "Generating JSON dump..."
    }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("DailyGrocer_Export", text)
        clipboard.setPrimaryClip(clip)
        onCopied()
    }

    fun shareExport(text: String, label: String) {
        val isJson = selectedTab == 3
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "DailyGrocer_${label}_${System.currentTimeMillis()}.${if (isJson) "json" else "csv"}")
            putExtra(Intent.EXTRA_TEXT, text)
            type = if (isJson) "application/json" else "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Export $label via")
        context.startActivity(shareIntent)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("📊 Data Export & JSON Backup", fontWeight = FontWeight.Bold)
                Text(
                    if (selectedTab == 3) "Full Room database dump for local/cloud backup" else "Export your grocery spending and Goli Soda logs",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
                    ) {
                        Text("Expenses", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
                    ) {
                        Text("Soda", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4)
                    ) {
                        Text("CSV All", fontSize = 10.sp, maxLines = 1)
                    }
                    SegmentedButton(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4)
                    ) {
                        Text("💾 JSON", fontSize = 10.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedTab == 3) "JSON Database Dump (Full Backup):" else "Preview Data:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (selectedTab == 3) {
                        OutlinedButton(
                            onClick = { showRestorePrompt = true },
                            modifier = Modifier.height(30.dp)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Restore JSON", fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    if (isGeneratingJson) {
                        Box(modifier = Modifier.height(140.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .horizontalScroll(rememberScrollState())
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = if (currentContent.isBlank()) "No records to export yet." else currentContent,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { copyToClipboard(currentContent) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedTab == 3) "Copy JSON" else "Copy CSV", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val label = when (selectedTab) {
                                0 -> "Expenses"
                                1 -> "Goli_Soda"
                                2 -> "All_Records"
                                else -> "Full_Database_Dump"
                            }
                            shareExport(currentContent, label)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (selectedTab == 3) "Share JSON" else "Share CSV", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    // Sub-dialog: Restore from JSON text
    if (showRestorePrompt) {
        AlertDialog(
            onDismissRequest = { showRestorePrompt = false },
            title = { Text("📥 Restore Database from JSON", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste a previously exported DailyGrocer JSON dump to restore groceries, transactions, and soda logs:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = restoreInputJson,
                        onValueChange = { restoreInputJson = it },
                        placeholder = { Text("Paste JSON dump here...") },
                        maxLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (restoreInputJson.isNotBlank()) {
                            isRestoring = true
                            scope.launch {
                                val res = DatabaseBackupManager.restoreDatabaseFromJson(
                                    restoreInputJson,
                                    groceryDao,
                                    cashbookDao,
                                    sodaDao
                                )
                                isRestoring = false
                                if (res.isSuccess) {
                                    val stats = res.getOrNull()
                                    Toast.makeText(
                                        context,
                                        "Restored: ${stats?.groceryCount} groceries, ${stats?.transactionCount} expenses, ${stats?.sodaSaleCount} soda sales!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    showRestorePrompt = false
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Restore failed: ${res.exceptionOrNull()?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = !isRestoring && restoreInputJson.isNotBlank()
                ) {
                    if (isRestoring) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Restore Now")
                    }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestorePrompt = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
