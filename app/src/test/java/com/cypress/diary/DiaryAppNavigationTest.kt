package com.cypress.diary

import com.cypress.diary.ui.navigation.DiaryRoute
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryAppNavigationTest {
    @Test
    fun bottomNavigationSelectionHandlesMissingRootRoute() {
        assertFalse(isBottomRouteSelected(null, DiaryRoute.Diary.route))
    }

    @Test
    fun bottomNavigationSelectionGroupsEditorRoutesUnderTheirRoots() {
        assertTrue(isBottomRouteSelected(DiaryRoute.Diary, DiaryRoute.Editor.route))
        assertTrue(isBottomRouteSelected(DiaryRoute.Ledger, DiaryRoute.AccountingEditor.route))
    }

    @Test
    fun githubSearchQueryOpensHiddenSettings() {
        assertTrue(isGitHubSettingsSearchQuery("github"))
        assertTrue(isGitHubSettingsSearchQuery(" GitHub "))
        assertFalse(isGitHubSettingsSearchQuery("github token"))
    }
}
