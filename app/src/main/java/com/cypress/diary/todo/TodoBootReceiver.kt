package com.cypress.diary.todo

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cypress.diary.storage.TodoItemStore

class TodoBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
        ) {
            return
        }
        val store = TodoItemStore(context.getSharedPreferences("todo_items", Context.MODE_PRIVATE))
        TodoReminderScheduler(context).scheduleAll(store.loadItems())
    }
}
