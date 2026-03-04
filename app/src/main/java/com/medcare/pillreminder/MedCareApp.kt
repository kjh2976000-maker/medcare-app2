package com.medcare.pillreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.media.AudioAttributes

class MedCareApp : Application() {
    companion object {
        const val CHANNEL_ID = "medcare_alarm"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "복약 알림",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "복약 시간 알림"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 100, 300, 100, 300)
            setBypassDnd(true)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
