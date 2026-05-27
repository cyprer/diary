package com.cypress.diary.todo

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cypress.diary.MainActivity
import com.cypress.diary.R
import com.cypress.diary.model.todo.TodoReminderMode

object TodoReminderNotifier {
    private const val ALARM_CHANNEL_ID = "todo_alarm_reminders"
    private const val NOTIFICATION_CHANNEL_ID = "todo_notification_reminders"
    private const val VIBRATION_CHANNEL_ID = "todo_vibration_reminders"
    private val VibrationPattern = longArrayOf(0L, 600L, 250L, 600L)

    fun show(context: Context, id: String, title: String, note: String, reminderMode: TodoReminderMode) {
        if (reminderMode == TodoReminderMode.Vibration) {
            vibrate(context)
        }
        if (!canPostNotifications(context)) return
        ensureChannel(context, reminderMode)

        val openIntent = if (reminderMode == TodoReminderMode.Alarm) {
            TodoAlarmActivity.intent(context, title, note)
        } else {
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            TodoReminderScheduler.requestCode(id),
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val body = note.ifBlank { "该处理这个待办了" }
        val builder = NotificationCompat.Builder(context, channelId(reminderMode))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${reminderMode.label}提醒：$title")
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(
                if (reminderMode == TodoReminderMode.Alarm) {
                    NotificationCompat.CATEGORY_ALARM
                } else {
                    NotificationCompat.CATEGORY_REMINDER
                },
            )
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)

        when (reminderMode) {
            TodoReminderMode.Alarm -> {
                builder
                    .setFullScreenIntent(contentIntent, true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
            }
            TodoReminderMode.Notification -> {
                builder.setDefaults(NotificationCompat.DEFAULT_SOUND)
            }
            TodoReminderMode.Vibration -> {
                builder
                    .setVibrate(VibrationPattern)
                    .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            }
        }

        NotificationManagerCompat.from(context)
            .notify(TodoReminderScheduler.requestCode(id), builder.build())
    }

    private fun canPostNotifications(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel(context: Context, reminderMode: TodoReminderMode) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            channelId(reminderMode),
            "${reminderMode.label}提醒",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "待办${reminderMode.label}提醒"
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            if (reminderMode == TodoReminderMode.Vibration) {
                vibrationPattern = VibrationPattern
                enableVibration(true)
            }
        }
        manager.createNotificationChannel(channel)
    }

    private fun channelId(reminderMode: TodoReminderMode): String {
        return when (reminderMode) {
            TodoReminderMode.Alarm -> ALARM_CHANNEL_ID
            TodoReminderMode.Notification -> NOTIFICATION_CHANNEL_ID
            TodoReminderMode.Vibration -> VIBRATION_CHANNEL_ID
        }
    }

    private fun vibrate(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(VibrationPattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VibrationPattern, -1)
        }
    }
}
