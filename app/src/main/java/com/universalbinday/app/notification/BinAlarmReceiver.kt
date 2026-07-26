package com.universalbinday.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.universalbinday.app.MainActivity
import com.universalbinday.app.R

class BinAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val binName = intent.getStringExtra(EXTRA_BIN_NAME) ?: "Bin"
        val container = intent.getStringExtra(EXTRA_CONTAINER) ?: "Bin"
        val color = intent.getIntExtra(EXTRA_COLOR, 0xFF2E7D32.toInt())

        val channelId = "bin_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.notification_channel_desc)
            }
            manager.createNotificationChannel(channel)
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            context, 0, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Bin night tomorrow")
            .setContentText("Put out your $binName ($container) tonight")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .setColor(color)
            .build()

        manager.notify(binName.hashCode(), notification)
    }

    companion object {
        const val EXTRA_BIN_NAME = "bin_name"
        const val EXTRA_CONTAINER = "container"
        const val EXTRA_COLOR = "color"
    }
}
