package com.cypress.diary.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
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
    data object Ledger : DiaryRoute("ledger", "账本", Icons.Filled.AccountBalanceWallet)
    data object AccountingStats : DiaryRoute("accounting_stats", "统计", Icons.Filled.BarChart)
    data object AccountingEditor : DiaryRoute("accounting_editor", "记一笔", Icons.Filled.Add)

    companion object {
        val diaryRootRoutes = listOf(Diary, Summary, Profile)
        val accountingRootRoutes = listOf(Ledger, AccountingStats, Profile)
        val rootRoutes = diaryRootRoutes

        fun rootRoutesFor(module: AppModule): List<DiaryRoute> {
            return when (module) {
                AppModule.Diary -> diaryRootRoutes
                AppModule.Accounting -> accountingRootRoutes
            }
        }
    }
}
