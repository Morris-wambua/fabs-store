package com.morrislabs.fabs_store.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.morrislabs.fabs_store.MainActivity
import com.morrislabs.fabs_store.R

object NotificationHelper {

    const val CHANNEL_CHAT = "fabs_store_chat"
    const val CHANNEL_RESERVATIONS = "fabs_store_reservations"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)

            val chatChannel = NotificationChannel(
                CHANNEL_CHAT,
                "Chat Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
            }

            val reservationsChannel = NotificationChannel(
                CHANNEL_RESERVATIONS,
                "Reservations",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for reservation updates"
            }

            manager.createNotificationChannel(chatChannel)
            manager.createNotificationChannel(reservationsChannel)
        }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String,
        notificationId: Int
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.fabs_store_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }
}
