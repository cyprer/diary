package com.cypress.diary.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AppBackgroundTest {
    @Test
    fun usesReadableButVisibleScrimWhenImageExists() {
        assertEquals(0.56f, appBackgroundScrimAlpha(hasImage = true), 0.001f)
    }

    @Test
    fun usesOpaqueBackgroundWhenImageIsMissing() {
        assertEquals(1f, appBackgroundScrimAlpha(hasImage = false), 0.001f)
    }
}
