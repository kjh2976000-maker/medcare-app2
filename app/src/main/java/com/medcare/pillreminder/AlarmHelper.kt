package com.medcare.pillreminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.medcare.pillreminder.data.DataStore
import com.medcare.pillreminder.data.Medication
import java.util.Calendar

class AlarmHelper(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAllAlarms() {
        val store = DataStore(context)
        val meds = store.loadMedications()
        // Cancel all existing alarms first
        for (i in 0 until 200) {
            cancelAlarm(i)
        }
        // Schedule for each active medication
        var requestCode = 0
        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun
        for (med in meds) {
            if (!med.days.contains(today)) continue
            // 10-minute advance alarm
            scheduleOne(med, requestCode, -10)
            requestCode++
            // Main alarm
            scheduleOne(med, requestCode, 0)
            requestCode++
            // Repeat alarms every 5 min for 30 min
            for (r in 1..6) {
                scheduleOne(med, requestCode, r * 5)
                requestCode++
            }
        }
    }

    private fun scheduleOne(med: Medication, requestCode: Int, offsetMinutes: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("med_name", med.name)
            putExtra("med_id", med.id)
            putExtra("med_dosage", med.dosage)
            putExtra("is_advance", offsetMinutes < 0)
            putExtra("is_repeat", offsetMinutes > 0)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, if (med.ampm == 0) {
                if (med.hour == 12) 0 else med.hour
            } else {
                if (med.hour == 12) 12 else med.hour + 12
            })
            set(Calendar.MINUTE, med.minute + offsetMinutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If time already passed today, skip
        if (calendar.timeInMillis <= System.currentTimeMillis()) return

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback for devices that don't allow exact alarms
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private fun cancelAlarm(requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}
