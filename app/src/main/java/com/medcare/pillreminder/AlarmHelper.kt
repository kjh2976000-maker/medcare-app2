package com.medcare.pillreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.medcare.pillreminder.data.DataStore
import java.util.Calendar

class AlarmHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarm(hour: Int, minute: Int) {
        // 시간 저장
        val prefs = context.getSharedPreferences("alarm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("alarm_hour", hour).putInt("alarm_minute", minute).apply()

        scheduleAlarmAt(hour, minute, 1)
    }

    fun scheduleAllAlarms() {
        // DataStore에서 약 목록 불러와서 알람 등록
        val dataStore = DataStore(context)
        val meds = dataStore.loadMedications()

        // 기존 알람 전부 취소
        for (i in 0..100) cancelAlarmByCode(i)

        // 각 약마다 알람 등록
        meds.forEachIndexed { index, med ->
            scheduleAlarmAt(med.hour, med.minute, index + 1, med.name, med.dosage, med.id)
        }
    }

    private fun scheduleAlarmAt(
        hour: Int,
        minute: Int,
        requestCode: Int,
        medName: String = "복약 시간",
        medDosage: String = "",
        medId: String = "1"
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("med_name", medName)
            putExtra("med_id", medId)
            putExtra("med_dosage", medDosage)
            putExtra("is_advance", false)
            putExtra("is_repeat", false)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            // 이미 지난 시간이면 다음날로
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarmByCode(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
