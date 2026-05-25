package com.cypress.diary.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePaletteTest {
    @Test
    fun exposesFourSimplePalettes() {
        assertEquals(4, ThemePalette.entries.size)
    }
}
