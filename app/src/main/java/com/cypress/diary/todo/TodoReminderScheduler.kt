package com.cypress.diary.todo

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cypress.diary.MainActivity
import com.cypress.diary.model.todo.TodoItem
import kotlin.math.abs

class TodoReminderScheduler(
    private val context: Context,
) {
    private val appContext = context.applicationContext

    fun sync(item: TodoItem) {
        if (shouldScheduleReminder(item)) {
            schedule(item)
        } else {
            cancel(item.id)
        }
    }

    fun scheduleAll(items: List<TodoItem>) {
        items.forEach(::sync)
    }

    fun cancel(id: String) {
        alarmManager.cancel(reminderPendingIntent(id, PendingIntent.FLAG_NO_CREATE))
    }

    private fun schedule(item: TodoItem) {
        val reminderAt = item.reminderAtMillis ?: return
        val showIntent = PendingIntent.getActivity(
            appContext,
            requestCode(item.id),
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            pendingIntentFlags(PendingIntent.FLAG_UPDATE_CURRENT),
        )
        val operation = reminderPendingIntent(item.id, PendingIntent.FLAG_UPDATE_CURRENT, item)
        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(reminderAt, showIntent),
            operation,
        )
    }

    private fun reminderPendingIntent(
        id: String,
        flags: Int,
        item: TodoItem? = null,
    ): PendingIntent {
        val intent = Intent(appContext, TodoReminderReceiver::class.java).apply {
            action = ACTION_TODO_REMINDER
            putExtra(EXTRA_TODO_ID, id)
            if (item != null) {
                putExtra(EXTRA_TODO_TITLE, item.title)
                putExtra(EXTRA_TODO_NOTE, item.note)
            }
        }
        return PendingIntent.getBroadcast(appContext, requestCode(id), intent, pendingIntentFlags(flags))
    }

    private val alarmManager: AlarmManager
        get() = appContext.getSystemService(AlarmManager::class.java)

    private fun pendingIntentFlags(flags: Int): Int {
        return flags or PendingIntent.FLAG_IMMUTABLE
    }

    companion object {
        const val ACTION_TODO_REMINDER = "com.cypress.diary.action.TODO_REMINDER"
        const val EXTRA_TODO_ID = "todo_id"
        const val EXTRA_TODO_TITLE = "todo_title"
        const val EXTRA_TODO_NOTE = "todo_note"

        fun requestCode(id: String): Int {
            return abs(id.hashCode())
        }
    }
}
