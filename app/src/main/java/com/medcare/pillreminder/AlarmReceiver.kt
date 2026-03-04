package com.medcare.pillreminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("med_name") ?: "약"
        val medDosage = intent.getStringExtra("med_dosage") ?: ""
        val medId = intent.getStringExtra("med_id") ?: "0"
        val isAdvance = intent.getBooleanExtra("is_advance", false)
        val isRepeat = intent.getBooleanExtra("is_repeat", false)

        val title = when {
            isAdvance -> "⏰ 10분 후 복약 시간"
            isRepeat -> "❗ 아직 약을 안 드셨어요"
            else -> "💊 복약 시간입니다"
        }

        val body = if (medDosage.isNotEmpty()) {
            "$medName - $medDosage"
        } else {
            medName
        }

        // Vibrate
        try {
            val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 100, 300, 100, 300), -1))
        } catch (_: Exception) {}

        // Open app intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context, medId.hashCode(), openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Full screen intent for lock screen
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("med_name", medName)
            putExtra("med_dosage", medDosage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        val fullScreenPending = PendingIntent.getActivity(
            context, medId.hashCode() + 1000, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedCareApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingOpen)
            .setFullScreenIntent(fullScreenPending, true)
            .setVibrate(longArrayOf(0, 300, 100, 300, 100, 300))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(medId.hashCode(), notification)
    }
}
