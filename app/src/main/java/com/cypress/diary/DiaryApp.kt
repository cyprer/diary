package com.cypress.diary

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.cypress.diary.ui.theme.DiaryTheme

@Composable
fun DiaryApp() {
    DiaryTheme {
        Scaffold(
            bottomBar = {
                // Bottom navigation will be added by the navigation task.
            }
        ) {
        }
    }
}
