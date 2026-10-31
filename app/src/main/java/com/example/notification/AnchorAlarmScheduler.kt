package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AnchorItem
import com.example.receiver.AnchorNotificationReceiver
import java.util.Calendar

object AnchorAlarmScheduler {

    private const val TAG = "AnchorAlarmScheduler"

    fun scheduleTaskReminder(context: Context, item: AnchorItem) {
        // Always clear the previous alarm first. An edit can move a reminder into the past or
        // remove its deadline, and keeping the old PendingIntent would fire stale task data.
        cancelTaskReminder(context, item.id)
        val targetMillis = calculateTaskTriggerMillis(item) ?: return
        if (targetMillis <= System.currentTimeMillis()) return

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_TASK_REMINDER
            putExtra(AnchorNotificationReceiver.EXTRA_TASK_ID, item.id)
            putExtra(AnchorNotificationReceiver.EXTRA_TASK_TITLE, item.title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            item.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        scheduleAlarm(alarmManager, targetMillis, pendingIntent)
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_TASK_REMINDER
            putExtra(AnchorNotificationReceiver.EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun scheduleDailyBriefs(context: Context, morningTime: String, eveningTime: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Morning Brief
        val morningMillis = calculateNextDailyMillis(morningTime)
        val morningIntent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_MORNING_BRIEF
        }
        val morningPending = PendingIntent.getBroadcast(
            context,
            88801,
            morningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarm(alarmManager, morningMillis, morningPending)

        // Evening Review
        val eveningMillis = calculateNextDailyMillis(eveningTime)
        val eveningIntent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_EVENING_REVIEW
        }
        val eveningPending = PendingIntent.getBroadcast(
            context,
            88802,
            eveningIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        scheduleAlarm(alarmManager, eveningMillis, eveningPending)
    }

    private fun scheduleAlarm(alarmManager: AlarmManager, triggerAtMillis: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException scheduling exact alarm, falling back to inexact: ${e.message}")
            try {
                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            } catch (inner: Exception) {
                Log.e(TAG, "Failed fallback alarm scheduling", inner)
            }
        }
    }

    fun calculateTaskTriggerMillis(item: AnchorItem): Long? {
        if (item.type != AnchorItem.TYPE_TASK || item.status == AnchorItem.STATUS_DONE) return null

        if (item.snoozeUntil != null && item.snoozeUntil > System.currentTimeMillis()) {
            return item.snoozeUntil
        }
        val dueDate = item.dueDate ?: return null
        val dueTime = item.dueTime ?: "09:00"

        return try {
            val dateParts = dueDate.split("-")
            val year = dateParts[0].toInt()
            val month = dateParts[1].toInt() - 1
            val day = dateParts[2].toInt()

            val timeParts = dueTime.split(":")
            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
            if (month !in 0..11 || day !in 1..31 || hour !in 0..23 || minute !in 0..59) return null

            val cal = Calendar.getInstance().apply {
                isLenient = false
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse trigger time for item ${item.id}", e)
            null
        }
    }

    fun calculateNextDailyMillis(timeStr: String): Long {
        val parts = timeStr.split(":")
        val parsedHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val parsedMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val hour = parsedHour.takeIf { it in 0..23 } ?: 8
        val minute = parsedMinute.takeIf { it in 0..59 } ?: 0

        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
