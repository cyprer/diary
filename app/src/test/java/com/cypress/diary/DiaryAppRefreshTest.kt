package com.cypress.diary

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiaryAppRefreshTest {
    @Test
    fun doesNotFetchRemoteOnInitialComposition() {
        assertFalse(shouldFetchRemoteDiary(refreshVersion = 0))
    }

    @Test
    fun fetchesRemoteAfterUserRefresh() {
        assertTrue(shouldFetchRemoteDiary(refreshVersion = 1))
    }

    @Test
    fun updatesDraftSnapshotAfterLocalEdit() {
        assertEquals(
            mapOf("draft-key" to "新的本地草稿"),
            updateDraftSnapshot(emptyMap(), "draft-key", "新的本地草稿"),
        )
    }

    @Test
    fun clearsDraftSnapshotAfterPush() {
        assertEquals(
            emptyMap<String, String>(),
            clearDraftSnapshot(mapOf("draft-key" to "已经推送的草稿"), "draft-key"),
        )
    }
}
