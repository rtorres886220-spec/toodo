package com.example.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.AnchorDatabase
import com.example.data.AnchorPreferences
import com.example.notification.AnchorAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val shouldReschedule = action in setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
            "android.intent.action.QUICKBOOT_POWERON"
        )
        if (!shouldReschedule) return

        if (action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S &&
                alarmManager?.canScheduleExactAlarms() != true
            ) return
        }

        NotificationHelper.createChannels(context)
        val prefs = AnchorPreferences.get(context).getSettings()
        AnchorAlarmScheduler.scheduleDailyBriefs(context, prefs.morningTime, prefs.eveningTime)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AnchorDatabase.getDatabase(context)
                val activeTasks = db.anchorDao().getAllActiveTasksSync()
                var scheduledCount = 0
                for (task in activeTasks) {
                    if (task.dueDate != null || task.snoozeUntil != null) {
                        AnchorAlarmScheduler.scheduleTaskReminder(context, task)
                        scheduledCount++
                    }
                }
                Log.d(TAG, "Restored $scheduledCount task reminders after $action.")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore task alarms after $action", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
