package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.ai.GeminiService
import com.example.data.AnchorDatabase
import com.example.data.AnchorItem
import com.example.data.AnchorPreferences
import com.example.data.AnchorRepository
import com.example.notification.AnchorAlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AnchorNotificationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AnchorReceiver"

        const val ACTION_DONE = "com.example.ACTION_DONE"
        const val ACTION_SNOOZE = "com.example.ACTION_SNOOZE"
        const val ACTION_TASK_REMINDER = "com.example.ACTION_TASK_REMINDER"
        const val ACTION_MORNING_BRIEF = "com.example.ACTION_MORNING_BRIEF"
        const val ACTION_EVENING_REVIEW = "com.example.ACTION_EVENING_REVIEW"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

        val db = AnchorDatabase.getDatabase(context)
        val repo = AnchorRepository(db.anchorDao(), GeminiService(), context.applicationContext)
        val prefs = AnchorPreferences.get(context)

        when (action) {
            ACTION_DONE -> {
                if (taskId != -1L) {
                    NotificationManagerCompat.from(context).cancel(taskId.toInt())
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val item = repo.getItem(taskId)
                            if (item != null) {
                                repo.markCompleted(item)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_SNOOZE -> {
                // Fixed 10m label vs actual duration: Snooze for exactly 10 minutes and reschedule alarm
                if (taskId != -1L) {
                    NotificationManagerCompat.from(context).cancel(taskId.toInt())
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val item = repo.getItem(taskId)
                            if (item != null && item.status != AnchorItem.STATUS_DONE) {
                                repo.snooze(item, hours = 0, minutes = 10)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_TASK_REMINDER -> {
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val item = repo.getItem(taskId)
                            if (item != null && item.status != AnchorItem.STATUS_DONE) {
                                // Respect Quiet Hours
                                if (isInsideQuietHours(prefs.quietHours)) {
                                    val delayEndMillis = calculateQuietHoursEndMillis(prefs.quietHours)
                                    val deferredItem = item.copy(snoozeUntil = delayEndMillis)
                                    db.anchorDao().update(deferredItem)
                                    AnchorAlarmScheduler.scheduleTaskReminder(context, deferredItem)
                                    Log.d(TAG, "Task $taskId deferred until quiet hours end.")
                                    return@launch
                                }

                                // Calculate escalation level based on user settings
                                val escalation = computeEscalationLevel(prefs.escalationIntensity, item.snoozeCount)
                                NotificationHelper.showTaskReminder(context, item.id, item.title, escalationLevel = escalation)
                            }
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
            ACTION_MORNING_BRIEF -> {
                NotificationHelper.showDailyBriefNotification(
                    context,
                    isMorning = true,
                    headline = "Ready to start your day? Tap to review your top 3 core anchors."
                )
                // Reschedule for next daily cycle
                val settings = prefs.getSettings()
                AnchorAlarmScheduler.scheduleDailyBriefs(context, settings.morningTime, settings.eveningTime)
            }
            ACTION_EVENING_REVIEW -> {
                NotificationHelper.showDailyBriefNotification(
                    context,
                    isMorning = false,
                    headline = "Time to wind down. Review what got done and clear your mind."
                )
                // Reschedule for next daily cycle
                val settings = prefs.getSettings()
                AnchorAlarmScheduler.scheduleDailyBriefs(context, settings.morningTime, settings.eveningTime)
            }
        }
    }

    private fun computeEscalationLevel(intensity: String, snoozeCount: Int): Int {
        return when (intensity.lowercase(java.util.Locale.getDefault())) {
            "off" -> 0
            "gentle" -> if (snoozeCount >= 3) 1 else 0
            "aggressive", "persistent" -> when {
                snoozeCount >= 3 -> 3
                snoozeCount >= 1 -> 2
                else -> 1
            }
            else -> when { // "Standard"
                snoozeCount >= 3 -> 2
                snoozeCount > 0 -> 1
                else -> 0
            }
        }
    }

    private fun isInsideQuietHours(quietHoursStr: String): Boolean {
        try {
            val parts = quietHoursStr.split("-").map { it.trim() }
            if (parts.size != 2) return false
            val startParts = parts[0].split(":").map { it.toInt() }
            val endParts = parts[1].split(":").map { it.toInt() }

            val cal = Calendar.getInstance()
            val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
            val startMinutes = startParts[0] * 60 + startParts[1]
            val endMinutes = endParts[0] * 60 + endParts[1]

            return if (startMinutes < endMinutes) {
                currentMinutes in startMinutes until endMinutes
            } else {
                currentMinutes >= startMinutes || currentMinutes < endMinutes
            }
        } catch (_: Exception) {
            return false
        }
    }

    private fun calculateQuietHoursEndMillis(quietHoursStr: String): Long {
        try {
            val parts = quietHoursStr.split("-").map { it.trim() }
            val endParts = parts[1].split(":").map { it.toInt() }
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, endParts[0])
                set(Calendar.MINUTE, endParts[1])
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            if (cal.timeInMillis <= System.currentTimeMillis()) {
                cal.add(Calendar.DAY_OF_YEAR, 1)
            }
            return cal.timeInMillis
        } catch (_: Exception) {
            return System.currentTimeMillis() + 60 * 60 * 1000L
        }
    }
}
