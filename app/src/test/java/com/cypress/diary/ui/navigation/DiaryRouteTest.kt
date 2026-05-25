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
}
