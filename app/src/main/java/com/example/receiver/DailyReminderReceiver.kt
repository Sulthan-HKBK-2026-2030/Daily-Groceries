package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.ReminderManager

class DailyReminderReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_DAILY_REMINDER = "com.example.ACTION_DAILY_REMINDER"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                // Device rebooted: re-register scheduled alarm if enabled
                if (ReminderManager.isReminderEnabled(context)) {
                    val hour = ReminderManager.getReminderHour(context)
                    val minute = ReminderManager.getReminderMinute(context)
                    ReminderManager.scheduleDailyReminder(context, hour, minute)
                }
            }

            ACTION_DAILY_REMINDER -> {
                // Time to fire reminder!
                if (ReminderManager.isReminderEnabled(context)) {
                    ReminderManager.showReminderNotification(context)

                    // Re-schedule for the next day at the same evening hour
                    val hour = ReminderManager.getReminderHour(context)
                    val minute = ReminderManager.getReminderMinute(context)
                    ReminderManager.scheduleDailyReminder(context, hour, minute)
                }
            }
        }
    }
}
