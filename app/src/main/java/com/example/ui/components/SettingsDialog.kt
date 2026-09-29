package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.cloud.FirebaseSyncManager
import com.example.data.cloud.SyncStatus
import com.example.data.local.AppDatabase
import com.example.util.DatabaseBackupManager
import com.example.util.ReminderManager
import com.example.util.ThemeMode
import com.example.util.ThemePreferenceManager
import kotlinx.coroutines.launch

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncManager = remember { FirebaseSyncManager.getInstance() }

    // Theme Mode
    val currentThemeMode by ThemePreferenceManager.themeMode.collectAsStateWithLifecycle()

    // Daily Reminder State
    var reminderEnabled by remember { mutableStateOf(ReminderManager.isReminderEnabled(context)) }
    var reminderHour by remember { mutableIntStateOf(ReminderManager.getReminderHour(context)) }
    var reminderMinute by remember { mutableIntStateOf(ReminderManager.getReminderMinute(context)) }

    // Firebase Auth & Sync State
    val currentUser by syncManager.currentUser.collectAsStateWithLifecycle()
    val syncStatus by syncManager.syncStatus.collectAsStateWithLifecycle()
    val isFirebaseAvailable by syncManager.isFirebaseAvailable.collectAsStateWithLifecycle()

    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isAuthSectionExpanded by remember { mutableStateOf(currentUser == null) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            reminderEnabled = true
            ReminderManager.setReminderSettings(context, true, reminderHour, reminderMinute)
            Toast.makeText(context, "Notifications enabled! Reminder scheduled for ${String.format("%02d:%02d", reminderHour, reminderMinute)}", Toast.LENGTH_SHORT).show()
        } else {
            reminderEnabled = false
            ReminderManager.setReminderSettings(context, false, reminderHour, reminderMinute)
            Toast.makeText(context, "Notification permission was denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val db = remember { AppDatabase.getDatabase(context) }
    val groceryDao = remember { db.groceryDao() }
    val cashbookDao = remember { db.cashbookDao() }
    val sodaDao = remember { db.sodaDao() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "⚙️", fontSize = 24.sp)
                Column {
                    Text("App Settings", fontWeight = FontWeight.Bold)
                    Text(
                        "Theme, Reminders & Cloud Sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ================= 1. THEME MODE TOGGLE =================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = when (currentThemeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Display Theme (Light / Dark)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = currentThemeMode == ThemeMode.LIGHT,
                                onClick = { ThemePreferenceManager.setThemeMode(context, ThemeMode.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                            ) {
                                Text("☀️ Light", fontSize = 11.sp, maxLines = 1)
                            }
                            SegmentedButton(
                                selected = currentThemeMode == ThemeMode.DARK,
                                onClick = { ThemePreferenceManager.setThemeMode(context, ThemeMode.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                            ) {
                                Text("🌙 Dark", fontSize = 11.sp, maxLines = 1)
                            }
                            SegmentedButton(
                                selected = currentThemeMode == ThemeMode.SYSTEM,
                                onClick = { ThemePreferenceManager.setThemeMode(context, ThemeMode.SYSTEM) },
                                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                            ) {
                                Text("📱 Auto", fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }

                // ================= 2. DAILY EVENING REMINDER =================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "Daily Evening Reminder",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Remind to log soda sales & grocery spend",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = reminderEnabled,
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                            if (!hasPermission) {
                                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                return@Switch
                                            }
                                        }
                                        reminderEnabled = true
                                        ReminderManager.setReminderSettings(context, true, reminderHour, reminderMinute)
                                    } else {
                                        reminderEnabled = false
                                        ReminderManager.setReminderSettings(context, false, reminderHour, reminderMinute)
                                    }
                                }
                            )
                        }

                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Reminder Time: ${String.format("%02d:%02d", reminderHour, reminderMinute)} (${if (reminderHour >= 12) "${if (reminderHour == 12) 12 else reminderHour - 12}:${String.format("%02d", reminderMinute)} PM" else "$reminderHour:${String.format("%02d", reminderMinute)} AM"})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            reminderHour = if (reminderHour <= 0) 23 else reminderHour - 1
                                            ReminderManager.setReminderSettings(context, true, reminderHour, reminderMinute)
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Text("-1h", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            reminderHour = if (reminderHour >= 23) 0 else reminderHour + 1
                                            ReminderManager.setReminderSettings(context, true, reminderHour, reminderMinute)
                                        },
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Text("+1h", fontSize = 10.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    ReminderManager.showReminderNotification(context)
                                    Toast.makeText(context, "Test notification sent!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Send Test Reminder Now", fontSize = 12.sp)
                            }
                        }
                    }
                }

                // ================= 3. FIREBASE AUTH & CLOUD SYNC =================
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = "Firebase Cloud Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sync groceries, transactions & budget across devices",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Current User State Banner
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (currentUser != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = currentUser?.email
                                            ?: if (currentUser?.isAnonymous == true) "Guest Account (Synced)" else "Not logged in (Local only)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }

                                if (currentUser != null) {
                                    OutlinedButton(
                                        onClick = { syncManager.signOut() },
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Sign Out", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Auth Form (if not logged in)
                        if (currentUser == null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = emailInput,
                                onValueChange = { emailInput = it },
                                label = { Text("Email", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = passwordInput,
                                onValueChange = { passwordInput = it },
                                label = { Text("Password", fontSize = 12.sp) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth()
                            )

                            authErrorMessage?.let { err ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = err,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Button(
                                    onClick = {
                                        authErrorMessage = null
                                        isAuthenticating = true
                                        syncManager.signInWithEmail(
                                            emailInput,
                                            passwordInput,
                                            onSuccess = {
                                                isAuthenticating = false
                                                Toast.makeText(context, "Signed in successfully!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isAuthenticating = false
                                                authErrorMessage = err
                                            }
                                        )
                                    },
                                    enabled = !isAuthenticating && emailInput.isNotBlank() && passwordInput.length >= 6,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Sign In", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        authErrorMessage = null
                                        isAuthenticating = true
                                        syncManager.signUpWithEmail(
                                            emailInput,
                                            passwordInput,
                                            onSuccess = {
                                                isAuthenticating = false
                                                Toast.makeText(context, "Account created!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isAuthenticating = false
                                                authErrorMessage = err
                                            }
                                        )
                                    },
                                    enabled = !isAuthenticating && emailInput.isNotBlank() && passwordInput.length >= 6,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Register", fontSize = 11.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedButton(
                                onClick = {
                                    authErrorMessage = null
                                    isAuthenticating = true
                                    syncManager.signInAnonymously(
                                        onSuccess = {
                                            isAuthenticating = false
                                            Toast.makeText(context, "Continuing as Guest!", Toast.LENGTH_SHORT).show()
                                        },
                                        onError = { err ->
                                            isAuthenticating = false
                                            authErrorMessage = err
                                        }
                                    )
                                },
                                enabled = !isAuthenticating,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Continue as Guest (No Password)", fontSize = 11.sp)
                            }
                        }

                        // Cloud Sync Action Buttons
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val res = syncManager.backupToCloud(groceryDao, cashbookDao, sodaDao)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Backup failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = currentUser != null && syncStatus !is SyncStatus.Syncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Backup", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val res = syncManager.restoreFromCloud(groceryDao, cashbookDao, sodaDao)
                                        if (res.isSuccess) {
                                            Toast.makeText(context, res.getOrNull(), Toast.LENGTH_LONG).show()
                                        } else {
                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Restore failed", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                enabled = currentUser != null && syncStatus !is SyncStatus.Syncing,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Restore", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = DatabaseBackupManager.exportDatabaseToJson(groceryDao, cashbookDao, sodaDao)
                                    DatabaseBackupManager.shareJsonDump(context, json)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("💾 Export Local JSON Database Dump", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Sync Status Banner
                        when (val status = syncStatus) {
                            is SyncStatus.Syncing -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Text("Syncing with Firestore...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            is SyncStatus.Success -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Text(status.message, style = MaterialTheme.typography.bodySmall, color = Color(0xFF2E7D32))
                                }
                            }
                            is SyncStatus.Error -> {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                    Text(status.errorMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
