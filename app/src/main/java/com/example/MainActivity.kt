package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.cloud.FirebaseSyncManager
import com.example.ui.DailyGrocerApp
import com.example.ui.theme.MyApplicationTheme
import com.example.util.ReminderManager
import com.example.util.ThemePreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Preferences, Reminder Channel, and Firebase Sync
        ThemePreferenceManager.init(this)
        ReminderManager.createNotificationChannel(this)
        if (ReminderManager.isReminderEnabled(this)) {
            ReminderManager.scheduleDailyReminder(
                this,
                ReminderManager.getReminderHour(this),
                ReminderManager.getReminderMinute(this)
            )
        }
        FirebaseSyncManager.getInstance().initialize(this)

        setContent {
            val currentThemeMode by ThemePreferenceManager.themeMode.collectAsStateWithLifecycle()

            MyApplicationTheme(themeMode = currentThemeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DailyGrocerApp()
                }
            }
        }
    }
}
