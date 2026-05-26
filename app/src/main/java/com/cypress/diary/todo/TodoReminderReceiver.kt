package com.cypress.diary.todo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TodoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TodoReminderScheduler.ACTION_TODO_REMINDER) return
        val id = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_ID) ?: return
        val title = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_TITLE).orEmpty()
        val note = intent.getStringExtra(TodoReminderScheduler.EXTRA_TODO_NOTE).orEmpty()
        TodoReminderNotifier.show(
            context = context,
            id = id,
            title = title.ifBlank { "待办" },
            note = note,
        )
    }
}
