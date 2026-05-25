package com.cypress.diary.ui.components

import androidx.compose.runtime.mutableStateMapOf

class WeekTreeExpansionState(
    private val expanded: MutableMap<String, Boolean> = mutableStateMapOf(),
) {
    fun isExpanded(key: String): Boolean = expanded[key] ?: false

    fun toggle(key: String) {
        expanded[key] = !isExpanded(key)
    }
}
