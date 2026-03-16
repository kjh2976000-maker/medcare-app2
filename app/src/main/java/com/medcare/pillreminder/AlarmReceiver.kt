package com.medcare.pillreminder

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val medName = intent.getStringExtra("med_name") ?: "복약 시간"
        val medDosage = intent.getStringExtra("med_dosage") ?: ""
        val medId = intent.getStringExtra("med_id") ?: "1"

        // AlarmActivity 실행 Intent
        val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("med_name", medName)
            putExtra("med_dosage", medDosage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val fullScreenPending = PendingIntent.getActivity(
            context,
            medId.hashCode() + 1000,
            alarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(context, AlarmActivity::class.java).apply {
            putExtra("med_name", medName)
            putExtra("med_dosage", medDosage)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            context,
            medId.hashCode(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MedCareApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("💊 복약 시간입니다")
            .setContentText(if (medDosage.isNotEmpty()) "$medName - $medDosage" else medName)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .setFullScreenIntent(fullScreenPending, true)
            .setVibrate(longArrayOf(0, 500, 500, 500, 500, 500))
            .setDefaults(NotificationCompat.DEFAULT_SOUND)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(medId.hashCode(), notification)

        // ✅ 수정: startActivity() 직접 호출 제거
        // → fullScreenIntent 알림이 자동으로 AlarmActivity를 띄워줍니다
    }
}
