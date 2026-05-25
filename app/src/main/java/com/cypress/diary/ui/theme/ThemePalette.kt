package com.cypress.diary.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

enum class ThemePalette(
    val label: String,
    val primary: Color,
    val secondary: Color,
    val surface: Color,
    val background: Color,
    val onSurface: Color,
) {
    BlueGray("蓝灰", Color(0xFF4D6B8A), Color(0xFFD9E3F1), Color(0xFFFBFCFE), Color(0xFFF6F8FB), Color(0xFF18212C)),
    Mint("薄荷绿", Color(0xFF3F6652), Color(0xFFD6E8DA), Color(0xFFFBFDF8), Color(0xFFF4F8F2), Color(0xFF191C1A)),
    Lavender("浅紫", Color(0xFF6B5B95), Color(0xFFE6DFF7), Color(0xFFFCFBFF), Color(0xFFF8F5FF), Color(0xFF1D1B20)),
    Graphite("深灰", Color(0xFF4A4F57), Color(0xFFDDE0E5), Color(0xFFFBFCFD), Color(0xFFF4F5F7), Color(0xFF17191C));

    fun lightScheme(): ColorScheme {
        return lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = secondary,
            onPrimaryContainer = onSurface,
            secondary = secondary,
            onSecondary = onSurface,
            secondaryContainer = secondary,
            onSecondaryContainer = onSurface,
            surface = surface,
            onSurface = onSurface,
            background = background,
            onBackground = onSurface,
        )
    }

    fun darkScheme(): ColorScheme {
        return darkColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.28f),
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = onSurface,
            secondaryContainer = secondary.copy(alpha = 0.24f),
            onSecondaryContainer = Color.White,
            surface = Color(0xFF1B1D20),
            onSurface = Color(0xFFE4E7EB),
            background = Color(0xFF111316),
            onBackground = Color(0xFFE4E7EB),
        )
    }
}
