package com.example.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class StudySphereMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM token: $token")
        // Token can be registered to user profile in Firestore
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        val title = remoteMessage.notification?.title 
            ?: remoteMessage.data["title"] 
            ?: "StudySphere Alert"
        val body = remoteMessage.notification?.body 
            ?: remoteMessage.data["body"] 
            ?: "You have a new update in your study community."
        val channelType = remoteMessage.data["type"] ?: "general"

        showNotification(title, body, channelType)
    }

    private fun showNotification(title: String, body: String, type: String) {
        val channelId = when (type) {
            "study" -> CHANNEL_STUDY_REMINDERS
            "social" -> CHANNEL_SOCIAL_ALERTS
            "achievement" -> CHANNEL_ACHIEVEMENTS
            else -> CHANNEL_DEFAULT
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getChannelName(channelId),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for StudySphere student platform"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun getChannelName(channelId: String): String {
        return when (channelId) {
            CHANNEL_STUDY_REMINDERS -> "Study Reminders & Goals"
            CHANNEL_SOCIAL_ALERTS -> "Social & Community Alerts"
            CHANNEL_ACHIEVEMENTS -> "Achievements & Level Ups"
            else -> "StudySphere General Notifications"
        }
    }

    companion object {
        private const val TAG = "StudySphereFCM"
        const val CHANNEL_DEFAULT = "studysphere_default_channel"
        const val CHANNEL_STUDY_REMINDERS = "studysphere_study_channel"
        const val CHANNEL_SOCIAL_ALERTS = "studysphere_social_channel"
        const val CHANNEL_ACHIEVEMENTS = "studysphere_achievements_channel"

        fun createNotificationChannels(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                val channels = listOf(
                    NotificationChannel(CHANNEL_STUDY_REMINDERS, "Study Reminders & Goals", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_SOCIAL_ALERTS, "Social & Community Alerts", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_ACHIEVEMENTS, "Achievements & Milestones", NotificationManager.IMPORTANCE_DEFAULT),
                    NotificationChannel(CHANNEL_DEFAULT, "General Notifications", NotificationManager.IMPORTANCE_DEFAULT)
                )
                channels.forEach { manager.createNotificationChannel(it) }
            }
        }
    }
}
