package com.cypress.diary.ui.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.FilterChip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cypress.diary.ui.state.clampDay
import java.time.LocalDate
import java.time.YearMonth

enum class DiaryCalendarMode(val label: String) {
    Year("年"),
    Month("月"),
    Week("周"),
}

@Composable
fun CalendarMonthPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val month = YearMonth.from(selectedDate)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onDateSelected(selectedDate.plusMonthsClamped(-1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上个月")
                }
                Text(
                    text = "${selectedDate.year}年${selectedDate.monthValue}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onDateSelected(selectedDate.plusMonthsClamped(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下个月")
                }
            }

            WeekHeader()

            calendarMonthCells(month).chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    week.forEach { date ->
                        DayCell(
                            date = date,
                            selected = date == selectedDate,
                            onDateSelected = onDateSelected,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

fun calendarMonthCells(month: YearMonth): List<LocalDate?> {
    val firstDay = month.atDay(1)
    val leadingBlankCount = firstDay.dayOfWeek.value % 7
    val days = (1..month.lengthOfMonth()).map { day -> month.atDay(day) }
    val unpadded = List(leadingBlankCount) { null } + days
    val trailingBlankCount = (7 - (unpadded.size % 7)).let { if (it == 7) 0 else it }
    return unpadded + List(trailingBlankCount) { null }
}

fun calendarWeekDates(date: LocalDate): List<LocalDate> {
    val sunday = date.minusDays((date.dayOfWeek.value % 7).toLong())
    return (0..6).map { sunday.plusDays(it.toLong()) }
}

fun previousCalendarWeekDate(date: LocalDate): LocalDate {
    return date.minusWeeks(1)
}

fun nextCalendarWeekDate(date: LocalDate): LocalDate {
    return date.plusWeeks(1)
}

fun calendarWeekMonthTitle(date: LocalDate): String {
    val month = calendarWeekDates(date)
        .groupingBy { YearMonth.from(it) }
        .eachCount()
        .maxByOrNull { it.value }!!
        .key
    return "${month.year}年${month.monthValue}月"
}

fun calendarYearMonths(year: Int): List<YearMonth> {
    return (1..12).map { month -> YearMonth.of(year, month) }
}

fun calendarMonthWeekStarts(month: YearMonth): List<LocalDate> {
    return (1..monthLocalWeekCount(month)).map { weekIndex ->
        month.atDay(monthLocalWeekStartDay(weekIndex))
    }
}

fun previousMonthLocalWeekDate(date: LocalDate): LocalDate {
    return date.plusMonthLocalWeeks(-1)
}

fun nextMonthLocalWeekDate(date: LocalDate): LocalDate {
    return date.plusMonthLocalWeeks(1)
}

private fun LocalDate.plusMonthLocalWeeks(delta: Int): LocalDate {
    val month = YearMonth.from(this)
    val weekIndex = monthLocalWeekIndex(dayOfMonth)
    val weekStartDay = monthLocalWeekStartDay(weekIndex)
    val offset = dayOfMonth - weekStartDay
    val targetMonth = when {
        delta < 0 && weekIndex == 1 -> month.minusMonths(1)
        delta > 0 && weekIndex == monthLocalWeekCount(month) -> month.plusMonths(1)
        else -> month
    }
    val targetWeekIndex = when {
        delta < 0 && weekIndex == 1 -> monthLocalWeekCount(targetMonth)
        delta > 0 && weekIndex == monthLocalWeekCount(month) -> 1
        else -> weekIndex + delta
    }
    val targetStartDay = monthLocalWeekStartDay(targetWeekIndex)
    val targetEndDay = minOf(targetStartDay + 6, targetMonth.lengthOfMonth())
    val targetDay = minOf(targetStartDay + offset, targetEndDay)
    return LocalDate.of(targetMonth.year, targetMonth.monthValue, targetDay)
}

private fun monthLocalWeekIndex(dayOfMonth: Int): Int {
    return ((dayOfMonth - 1) / 7) + 1
}

private fun monthLocalWeekStartDay(weekIndex: Int): Int {
    return ((weekIndex - 1) * 7) + 1
}

private fun monthLocalWeekCount(month: YearMonth): Int {
    return monthLocalWeekIndex(month.lengthOfMonth())
}

@Composable
fun CalendarModeTabs(
    selectedMode: DiaryCalendarMode,
    onModeSelected: (DiaryCalendarMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DiaryCalendarMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(mode.label) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun CalendarYearPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onDateSelected(selectedDate.withYear(selectedDate.year - 1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一年")
                }
                Text(
                    text = "${selectedDate.year}年",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onDateSelected(selectedDate.withYear(selectedDate.year + 1)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一年")
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ((selectedDate.year - 1)..(selectedDate.year + 1)).forEach { year ->
                    Text(
                        text = "${year}年",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    calendarYearMonths(year).chunked(3).forEach { rowMonths ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            rowMonths.forEach { month ->
                                MonthCell(
                                    month = month,
                                    selected = month.year == selectedDate.year && month.monthValue == selectedDate.monthValue,
                                    onClick = {
                                        val day = clampDay(month.year, month.monthValue, selectedDate.dayOfMonth)
                                        onDateSelected(LocalDate.of(month.year, month.monthValue, day))
                                    },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalendarWeekPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = 1) { 3 }

    LaunchedEffect(selectedDate) {
        if (pagerState.currentPage != 1) {
            pagerState.scrollToPage(1)
        }
    }

    LaunchedEffect(pagerState, selectedDate) {
        snapshotFlow { pagerState.currentPage to pagerState.isScrollInProgress }
            .collect { (page, scrolling) ->
                if (!scrolling && page != 1) {
                    onDateSelected(
                        when (page) {
                            0 -> previousCalendarWeekDate(selectedDate)
                            else -> nextCalendarWeekDate(selectedDate)
                        },
                    )
                    pagerState.scrollToPage(1)
                }
            }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = { onDateSelected(previousCalendarWeekDate(selectedDate)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一周")
                }
                Text(
                    text = calendarWeekMonthTitle(selectedDate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                IconButton(onClick = { onDateSelected(nextCalendarWeekDate(selectedDate)) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一周")
                }
            }

            WeekHeader()
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) {
                val pageDate = when (it) {
                    0 -> previousCalendarWeekDate(selectedDate)
                    1 -> selectedDate
                    else -> nextCalendarWeekDate(selectedDate)
                }
                WeekDateRow(
                    dates = calendarWeekDates(pageDate),
                    selectedDate = selectedDate,
                    onDateSelected = onDateSelected,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun WeekDateRow(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        dates.forEach { date ->
            DayCell(
                date = date,
                selected = date == selectedDate,
                onDateSelected = onDateSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WeekHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        listOf("周日", "周一", "周二", "周三", "周四", "周五", "周六").forEach { label ->
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MonthCell(
    month: YearMonth,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.aspectRatio(1.55f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
        tonalElevation = if (selected) 0.dp else 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "${month.monthValue}月",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    selected: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        if (date != null) {
            Surface(
                onClick = { onDateSelected(date) },
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

private fun LocalDate.plusMonthsClamped(delta: Long): LocalDate {
    val nextMonth = YearMonth.from(this).plusMonths(delta)
    val nextDay = clampDay(nextMonth.year, nextMonth.monthValue, dayOfMonth)
    return LocalDate.of(nextMonth.year, nextMonth.monthValue, nextDay)
}
