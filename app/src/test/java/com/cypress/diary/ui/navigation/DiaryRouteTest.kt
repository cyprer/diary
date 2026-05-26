package com.cypress.diary.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class DiaryRouteTest {
    @Test
    fun rootRoutesExposeThreeNonNullItems() {
        assertEquals(
            listOf("diary", "summary", "profile"),
            DiaryRoute.rootRoutes.map { it.route },
        )
    }

    @Test
    fun diaryRootRoutesMatchDiaryWorkspace() {
        assertEquals(
            listOf(DiaryRoute.Diary, DiaryRoute.Summary, DiaryRoute.Profile),
            DiaryRoute.rootRoutesFor(AppModule.Diary),
        )
    }

    @Test
    fun accountingRootRoutesMatchAccountingWorkspace() {
        assertEquals(
            listOf(DiaryRoute.Ledger, DiaryRoute.AccountingStats, DiaryRoute.Profile),
            DiaryRoute.rootRoutesFor(AppModule.Accounting),
        )
    }

    @Test
    fun todoRootRoutesMatchTodoWorkspace() {
        assertEquals(
            listOf(DiaryRoute.TodoList, DiaryRoute.Profile),
            DiaryRoute.rootRoutesFor(AppModule.Todo),
        )
    }
}
