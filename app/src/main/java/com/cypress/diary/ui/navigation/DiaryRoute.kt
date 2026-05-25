package com.cypress.diary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class DiaryRoute(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    data object Diary : DiaryRoute("diary", "日记", Icons.Filled.Home)
    data object Summary : DiaryRoute("summary", "总结", Icons.Filled.DateRange)
    data object Profile : DiaryRoute("profile", "我的", Icons.Filled.Person)
    data object Editor : DiaryRoute("editor", "编辑", Icons.Filled.Edit)

    companion object {
        val rootRoutes = listOf(Diary, Summary, Profile)
    }
}
