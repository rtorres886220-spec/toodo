package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.AnchorItem
import com.example.receiver.AnchorNotificationReceiver

object NotificationHelper {

    const val CHANNEL_REMINDERS = "channel_anchor_reminders"
    const val CHANNEL_ESCALATION = "channel_anchor_escalation"
    const val CHANNEL_CHECKIN = "channel_anchor_checkin"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                CHANNEL_REMINDERS,
                "Anchor Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Task notifications and reminders"
                enableVibration(true)
            }

            val escalationChannel = NotificationChannel(
                CHANNEL_ESCALATION,
                "Escalation Ladder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Repeated escalation alerts for high-priority anchors"
                enableVibration(true)
            }

            val checkinChannel = NotificationChannel(
                CHANNEL_CHECKIN,
                "Dead-App & Daily Check-Ins",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Gentle check-ins to keep you anchored"
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, escalationChannel, checkinChannel))
        }
    }

    fun showTaskReminder(
        context: Context,
        taskId: Long,
        title: String,
        escalationLevel: Int = 0
    ) {
        createChannels(context)

        // Open app intent
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("taskId", taskId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Done action
        val doneIntent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_DONE
            putExtra(AnchorNotificationReceiver.EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Snooze 10m action
        val snoozeIntent = Intent(context, AnchorNotificationReceiver::class.java).apply {
            action = AnchorNotificationReceiver.ACTION_SNOOZE
            putExtra(AnchorNotificationReceiver.EXTRA_TASK_ID, taskId)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = if (escalationLevel > 0) CHANNEL_ESCALATION else CHANNEL_REMINDERS
        val contentText = when (escalationLevel) {
            0 -> "Ready to anchor this down?"
            1 -> "Still on your deck: knock this out in 5 minutes."
            2 -> "High priority alert: Take action now or reschedule."
            else -> "Urgent focus: Let's finish '$title' right now."
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_stat_anchor)
            .setContentTitle(if (escalationLevel > 0) "Anchor Escalation: $title" else "Anchor: $title")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.checkbox_on_background, "Done", donePendingIntent)
            .addAction(android.R.drawable.ic_popup_reminder, "Snooze 10m", snoozePendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
        } catch (e: SecurityException) {
            // In case notification permission not granted
        }
    }

    fun showDeadAppCheckIn(context: Context, hoursInactive: Int) {
        createChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            99999,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CHECKIN)
            .setSmallIcon(R.drawable.ic_stat_anchor)
            .setContentTitle("Anchor Check-In")
            .setContentText("It's been $hoursInactive hours. Keep your mind clear—capture your thoughts or review today's top 3.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(99999, notification)
        } catch (e: SecurityException) {
            // Permission missing
        }
    }

    fun showDailyBriefNotification(context: Context, isMorning: Boolean, headline: String) {
        createChannels(context)

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("action", if (isMorning) "open_morning_brief" else "open_evening_review")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            if (isMorning) 8881 else 8882,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isMorning) "Morning Anchor Brief" else "Evening Focus Review"
        val notification = NotificationCompat.Builder(context, CHANNEL_CHECKIN)
            .setSmallIcon(R.drawable.ic_stat_anchor)
            .setContentTitle(title)
            .setContentText(headline)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(if (isMorning) 8881 else 8882, notification)
        } catch (_: SecurityException) {
        }
    }
}

