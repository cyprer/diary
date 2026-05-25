package com.cypress.diary.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cypress.diary.ui.calendar.CalendarModeTabs
import com.cypress.diary.ui.calendar.CalendarMonthPicker
import com.cypress.diary.ui.calendar.CalendarWeekPicker
import com.cypress.diary.ui.calendar.CalendarYearPicker
import com.cypress.diary.ui.calendar.DiaryCalendarMode
import com.cypress.diary.ui.components.DiaryCard
import com.cypress.diary.ui.components.RefreshableScreen
import com.cypress.diary.ui.search.DiarySearchResult
import java.time.LocalDate

@Composable
fun DiaryScreen(
    date: LocalDate,
    body: String?,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onDateChange: (LocalDate) -> Unit,
    calendarMode: DiaryCalendarMode,
    onCalendarModeChange: (DiaryCalendarMode) -> Unit,
    quote: String?,
    searchQuery: String,
    searchResults: List<DiarySearchResult>,
    onSearchQueryChange: (String) -> Unit,
    onSearchResultSelected: (DiarySearchResult) -> Unit,
    modifier: Modifier = Modifier,
) {
    RefreshableScreen(
        refreshing = refreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SearchBox(
            query = searchQuery,
            results = searchResults,
            onQueryChange = onSearchQueryChange,
            onResultSelected = onSearchResultSelected,
        )

        CalendarModeTabs(
            selectedMode = calendarMode,
            onModeSelected = onCalendarModeChange,
        )

        CalendarSwitcher(
            mode = calendarMode,
            date = date,
            onDateChange = onDateChange,
            onCalendarModeChange = onCalendarModeChange,
        )

        if (calendarMode != DiaryCalendarMode.Year) {
            Text(
                text = quote ?: dailyQuote(date),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier,
            )

            DiaryCard(body = body)
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun CalendarSwitcher(
    mode: DiaryCalendarMode,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    onCalendarModeChange: (DiaryCalendarMode) -> Unit,
) {
    AnimatedContent(
        targetState = mode,
        transitionSpec = {
            (fadeIn() + scaleIn(initialScale = 0.96f)) togetherWith
                (fadeOut() + scaleOut(targetScale = 0.96f))
        },
        label = "diary-calendar-mode",
    ) { targetMode ->
        when (targetMode) {
            DiaryCalendarMode.Year -> CalendarYearPicker(
                selectedDate = date,
                onDateSelected = { selected ->
                    onDateChange(selected)
                    onCalendarModeChange(DiaryCalendarMode.Month)
                },
            )
            DiaryCalendarMode.Month -> CalendarMonthPicker(
                selectedDate = date,
                onDateSelected = onDateChange,
            )
            DiaryCalendarMode.Week -> CalendarWeekPicker(
                selectedDate = date,
                onDateSelected = onDateChange,
            )
        }
    }
}

@Composable
private fun SearchBox(
    query: String,
    results: List<DiarySearchResult>,
    onQueryChange: (String) -> Unit,
    onResultSelected: (DiarySearchResult) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null)
            },
            placeholder = { Text("搜索日记") },
        )

        if (query.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                if (results.isEmpty()) {
                    Text(
                        text = "没有找到相关记录",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    )
                } else {
                    Column {
                        results.forEachIndexed { index, result ->
                            SearchResultRow(
                                result = result,
                                onClick = { onResultSelected(result) },
                            )
                            if (index != results.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: DiarySearchResult,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${result.date.year}年${result.date.monthValue}月${result.date.dayOfMonth}日",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = result.excerpt.ifBlank { "空白日记" },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun dailyQuote(date: LocalDate): String {
    val quotes = listOf(
        "把今天写下来，未来就有路标。",
        "慢慢来，最稳的进步都不急。",
        "今天的认真，会在明天发光。",
        "记录不是负担，是整理生活。",
        "你已经在往前走了，只是过程很安静。",
    )
    return quotes[date.toEpochDay().toInt().floorMod(quotes.size)]
}

private fun Int.floorMod(modulus: Int): Int {
    val result = this % modulus
    return if (result < 0) result + modulus else result
}
