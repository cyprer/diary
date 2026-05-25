package com.cypress.diary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun DiaryTheme(
    palette: ThemePalette = ThemePalette.BlueGray,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) palette.darkScheme() else palette.lightScheme(),
        typography = MaterialTheme.typography,
        content = content,
    )
}
