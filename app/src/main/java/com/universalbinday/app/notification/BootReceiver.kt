package com.universalbinday.app.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Re-schedules alarms after device reboot.
 * Actual reschedule logic is triggered from MainActivity / ViewModel on next launch,
 * but this ensures we can react if needed.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Alarms will be re-scheduled when the app next starts or when settings change.
            // For a more robust version you could inject a WorkManager here.
        }
    }
}
