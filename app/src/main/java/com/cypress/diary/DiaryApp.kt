package com.cypress.diary

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.cypress.diary.ui.theme.DiaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryApp() {
    DiaryTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(text = "Diary")
                    }
                )
            },
            bottomBar = {
                // Bottom navigation will be added by the navigation task.
            }
        ) { innerPadding ->
            Text(
                text = "Diary app",
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
