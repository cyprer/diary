package com.cypress.diary.ui.navigation

enum class AppModule(
    val label: String,
) {
    Diary("日记"),
    Accounting("记账"),
    Todo("待办");

    companion object {
        val selectable: List<AppModule>
            get() = listOf(Diary, Accounting)
    }
}
