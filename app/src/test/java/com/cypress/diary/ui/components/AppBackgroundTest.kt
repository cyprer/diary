package com.cypress.diary.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AppBackgroundTest {
    @Test
    fun usesReadableButVisibleScrimWhenImageExists() {
        assertEquals(0.56f, appBackgroundScrimAlpha(hasImage = true, layoutOpacity = 1f), 0.001f)
    }

    @Test
    fun lowersScrimWhenLayoutOpacityIsLowered() {
        assertEquals(0.3f, appBackgroundScrimAlpha(hasImage = true, layoutOpacity = 0.35f), 0.001f)
    }

    @Test
    fun usesOpaqueBackgroundWhenImageIsMissing() {
        assertEquals(1f, appBackgroundScrimAlpha(hasImage = false), 0.001f)
    }
}
