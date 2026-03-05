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
        val medName = intent.getStringExtra("med_name") ?: ""
        val medDosage = intent.getStringExtra("med_dosage") ?: ""
        val medId = intent.getStringExtra("med_id") ?: "0"
        val isAdvance = intent.getBooleanExtra("is_advance", false)
        val isRepeat = intent.getBooleanExtra("is_repeat", false)

        val title = when {
            isAdvance -> "💊 약 복용 30분 전입니다"
            isRepeat -> "⏰ 아직 약을 드셨나요?"
            else -> "💊 약 복용 시간입니다"
        }

        val body = if (medDosage.isNotEmpty()) {
            "$medName $medDosage"
        } else {
            medName
        }

        // 진동
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 100, 500, 100, 500), -1))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 500, 100, 500, 100, 500), -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 알림
        val fullScreenIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("med_name", medName)
            putExtra("med_dosage", medDosage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        val fullScreenPending = PendingIntent.getActivity(
            context, medId.hashCode() * 1000, fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedCareApp.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(fullScreenPending)
            .setFullScreenIntent(fullScreenPending, true)
            .setVibrate(longArrayOf(0, 500, 100, 500, 100, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(medId.hashCode(), notification)
    }
}
