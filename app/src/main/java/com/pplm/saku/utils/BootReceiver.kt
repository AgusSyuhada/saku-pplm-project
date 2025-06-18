package com.pplm.saku.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val appPreferences = AppPreferences(context)
            if (appPreferences.isDailyReminderEnabled()) {
                val timeString = appPreferences.getReminderTime()
                val parts = timeString.split(":")
                if (parts.size == 2) {
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    AlarmHelper(context).setDailyReminder(hour, minute)
                }
            }
        }
    }
}