package com.cypress.diary

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.cypress.diary.github.GitHubConfig
import com.cypress.diary.github.GitHubConfigStore
import com.cypress.diary.github.GitHubDiaryRepository
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.parser.DiaryDocumentCodec
import com.cypress.diary.parser.DiaryMarkdownCodec
import com.cypress.diary.parser.WeekPathResolver
import com.cypress.diary.quote.DailyQuoteRepository
import com.cypress.diary.storage.AppAppearanceStore
import com.cypress.diary.storage.DailyQuoteStore
import com.cypress.diary.storage.DiaryDocumentCacheStore
import com.cypress.diary.storage.DiaryWeekCacheStore
import com.cypress.diary.storage.EditorDraftStore
import com.cypress.diary.storage.SharedPreferencesPreferenceStore
import com.cypress.diary.ui.components.AppBackground
import com.cypress.diary.ui.navigation.DiaryRoute
import com.cypress.diary.ui.sample.sampleDiaryWeeks
import com.cypress.diary.ui.screens.DiaryScreen
import com.cypress.diary.ui.screens.EditorScreen
import com.cypress.diary.ui.screens.EditMode
import com.cypress.diary.ui.screens.ProfileScreen
import com.cypress.diary.ui.screens.SummaryScreen
import com.cypress.diary.ui.calendar.DiaryCalendarMode
import com.cypress.diary.ui.editor.DraftContentResolver
import com.cypress.diary.ui.editor.DiaryEditContentBuilder
import com.cypress.diary.ui.search.searchDiaryWeeks
import com.cypress.diary.ui.theme.DiaryTheme
import com.cypress.diary.ui.theme.ThemePalette
import java.time.LocalDate
import kotlinx.coroutines.launch

@Composable
fun DiaryApp() {
    val context = LocalContext.current.applicationContext
    val configStore = remember(context) { GitHubConfigStore(context) }
    val appearanceStore = remember(context) {
        AppAppearanceStore(context.getSharedPreferences("app_appearance", android.content.Context.MODE_PRIVATE))
    }
    val weekCacheStore = remember(context) {
        DiaryWeekCacheStore(
            SharedPreferencesPreferenceStore(
                context.getSharedPreferences("week_cache", android.content.Context.MODE_PRIVATE),
            ),
        )
    }
    val documentCacheStore = remember(context) {
        DiaryDocumentCacheStore(
            SharedPreferencesPreferenceStore(
                context.getSharedPreferences("document_cache", android.content.Context.MODE_PRIVATE),
            ),
        )
    }
    val draftStore = remember(context) {
        EditorDraftStore(context.getSharedPreferences("editor_drafts", android.content.Context.MODE_PRIVATE))
    }
    val quoteStore = remember(context) {
        DailyQuoteStore(context.getSharedPreferences("daily_quotes", android.content.Context.MODE_PRIVATE))
    }
    val repository = remember { GitHubDiaryRepository() }
    val quoteRepository = remember { DailyQuoteRepository() }
    val sampleWeeks = remember { sampleDiaryWeeks() }
    val editorBuilder = remember { DiaryEditContentBuilder() }
    val draftContentResolver = remember { DraftContentResolver() }
    val documentCodec = remember { DiaryDocumentCodec() }
    val weekCodec = remember { DiaryMarkdownCodec() }
    val weekPathResolver = remember { WeekPathResolver() }
    val scope = rememberCoroutineScope()

    var appearance by remember { mutableStateOf(appearanceStore.load()) }
    var githubConfig by remember { mutableStateOf(configStore.load()) }
    var connectionStatus by rememberSaveable { mutableStateOf("未连接") }
    var remoteDocuments by remember { mutableStateOf<List<DiaryDocument>?>(null) }
    var cachedWeeks by remember { mutableStateOf<List<DiaryWeek>?>(weekCacheStore.loadWeeks().ifEmpty { null }) }
    var cachedDocuments by remember { mutableStateOf<List<DiaryDocument>?>(documentCacheStore.loadDocuments().ifEmpty { null }) }
    var draftSnapshots by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var refreshing by rememberSaveable { mutableStateOf(false) }
    var refreshVersion by rememberSaveable { mutableStateOf(0) }

    var selectedDateValue by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var route by rememberSaveable { mutableStateOf(DiaryRoute.Diary.route) }
    var editorModeName by rememberSaveable { mutableStateOf(EditMode.Day.name) }
    var selectedSummaryPath by rememberSaveable { mutableStateOf<String?>(null) }
    var editorDocumentPath by rememberSaveable { mutableStateOf<String?>(null) }
    var dailyQuote by rememberSaveable { mutableStateOf<String?>(null) }
    var diarySearchQuery by rememberSaveable { mutableStateOf("") }
    var diaryCalendarModeName by rememberSaveable { mutableStateOf(DiaryCalendarMode.Month.name) }

    val selectedDate = LocalDate.parse(selectedDateValue)
    val sampleDocuments = remember(sampleWeeks) {
        sampleWeeks.mapNotNull { week ->
            val path = weekPathResolver.resolve(week.key)
            runCatching { documentCodec.parse(path, editorBuilder.render(week)) }.getOrNull()
        }
    }
    val activeDocuments = remoteDocuments ?: cachedDocuments ?: sampleDocuments
    val activeWeeks = remember(activeDocuments, cachedWeeks, sampleWeeks) {
        documentsToWeeks(activeDocuments, weekCodec).ifEmpty { cachedWeeks ?: sampleWeeks }
    }
    val selectedWeek = remember(activeWeeks, selectedDateValue) { activeWeeks.findByDate(selectedDate) }
    val palette = resolvePalette(appearance.paletteName)
    val editorMode = EditMode.valueOf(editorModeName)
    val editorPath = remember(selectedDateValue) { weekPathResolver.resolve(WeekKey.from(selectedDate)) }
    val selectedDayDraftKey = remember(editorPath, selectedDateValue) {
        editorDraftKey(editorPath, EditMode.Day, selectedDate)
    }
    val selectedDayBody = draftContentResolver.resolveDayBody(
        remoteBody = selectedWeek?.days?.firstOrNull { it.date == selectedDate }?.content,
        localDraft = draftSnapshots[selectedDayDraftKey] ?: draftStore.load(selectedDayDraftKey),
    )
    val diarySearchResults = remember(activeWeeks, diarySearchQuery) {
        searchDiaryWeeks(activeWeeks, diarySearchQuery)
    }
    val diaryCalendarMode = DiaryCalendarMode.valueOf(diaryCalendarModeName)
    val selectedSummaryBaseDocument = remember(activeDocuments, selectedSummaryPath) {
        selectedSummaryPath?.let { path -> activeDocuments.firstOrNull { it.path == path } }
    }
    val selectedSummaryDocument = draftContentResolver.resolveDocument(
        document = selectedSummaryBaseDocument,
        localDraft = selectedSummaryBaseDocument?.let { document ->
            val key = documentDraftKey(document.path)
            draftSnapshots[key] ?: draftStore.load(key)
        },
    )
    val summaryEditorDocument = remember(activeDocuments, editorDocumentPath) {
        editorDocumentPath?.let { path -> activeDocuments.firstOrNull { it.path == path } }
    }
    val editorKey = remember(selectedDateValue, editorModeName, editorDocumentPath) {
        editorDocumentPath?.let(::documentDraftKey) ?: editorDraftKey(editorPath, editorMode, selectedDate)
    }
    var editorDraft by rememberSaveable(editorKey) {
        mutableStateOf(
            if (editorDocumentPath != null) {
                draftStore.load(editorKey) ?: summaryEditorDocument?.markdown.orEmpty()
            } else {
                loadInitialEditorDraft(editorBuilder, draftStore, editorPath, selectedDate, selectedWeek, editorMode)
            },
        )
    }

    fun persistAppearance(updatedPalette: ThemePalette = palette, backgroundUri: String? = appearance.backgroundUri) {
        val next = appearance.copy(
            paletteName = updatedPalette.name,
            backgroundUri = backgroundUri,
        )
        appearanceStore.save(next.paletteName, next.backgroundUri)
        appearance = next
    }

    fun refreshWeeks() {
        refreshVersion += 1
    }

    LaunchedEffect(refreshVersion) {
        if (!shouldFetchRemoteDiary(refreshVersion)) {
            return@LaunchedEffect
        }
        refreshing = true
        val config = githubConfig
        if (config == null) {
            remoteDocuments = null
            connectionStatus = "未连接"
            refreshing = false
            return@LaunchedEffect
        }

        connectionStatus = "正在连接 ${config.owner}/${config.repo}..."
        runCatching {
            repository.loadDocuments(config)
        }.onSuccess { documents ->
            val weeks = documentsToWeeks(documents, weekCodec)
            remoteDocuments = documents
            cachedDocuments = documents
            cachedWeeks = weeks
            documentCacheStore.saveDocuments(documents)
            weekCacheStore.saveWeeks(weeks)
            connectionStatus = "已连接 ${config.owner}/${config.repo}@${config.branch}，共 ${documents.size} 个总结文档"
        }.onFailure { error ->
            remoteDocuments = null
            connectionStatus = "连接失败：${error.message ?: "未知错误"}"
        }
        refreshing = false
    }

    LaunchedEffect(selectedDateValue) {
        val cachedQuote = quoteStore.load(selectedDate)
        if (cachedQuote != null) {
            dailyQuote = cachedQuote
            return@LaunchedEffect
        }

        dailyQuote = null
        runCatching {
            quoteRepository.fetch()
        }.onSuccess { quote ->
            quoteStore.save(selectedDate, quote)
            dailyQuote = quote
        }
    }

    DiaryTheme(palette = palette) {
        AppBackground(backgroundUri = appearance.backgroundUri) {
            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar {
                        NavigationBarItem(
                            selected = route == DiaryRoute.Diary.route || route == DiaryRoute.Editor.route,
                            onClick = { route = DiaryRoute.Diary.route },
                            icon = {
                                Icon(
                                    imageVector = DiaryRoute.Diary.icon,
                                    contentDescription = DiaryRoute.Diary.label,
                                )
                            },
                            label = { Text(DiaryRoute.Diary.label) },
                        )
                        NavigationBarItem(
                            selected = route == DiaryRoute.Summary.route,
                            onClick = { route = DiaryRoute.Summary.route },
                            icon = {
                                Icon(
                                    imageVector = DiaryRoute.Summary.icon,
                                    contentDescription = DiaryRoute.Summary.label,
                                )
                            },
                            label = { Text(DiaryRoute.Summary.label) },
                        )
                        NavigationBarItem(
                            selected = route == DiaryRoute.Profile.route,
                            onClick = { route = DiaryRoute.Profile.route },
                            icon = {
                                Icon(
                                    imageVector = DiaryRoute.Profile.icon,
                                    contentDescription = DiaryRoute.Profile.label,
                                )
                            },
                            label = { Text(DiaryRoute.Profile.label) },
                        )
                    }
                },
                floatingActionButton = {
                    if (route == DiaryRoute.Diary.route) {
                        FloatingActionButton(
                            onClick = {
                                editorDocumentPath = null
                                route = DiaryRoute.Editor.route
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "编辑",
                            )
                        }
                    }
                },
            ) { innerPadding ->
                when (route) {
                    DiaryRoute.Diary.route -> DiaryScreen(
                        date = selectedDate,
                        body = selectedDayBody,
                        refreshing = refreshing,
                        onRefresh = { refreshWeeks() },
                        onDateChange = { date -> selectedDateValue = date.toString() },
                        calendarMode = diaryCalendarMode,
                        onCalendarModeChange = { mode -> diaryCalendarModeName = mode.name },
                        quote = dailyQuote,
                        searchQuery = diarySearchQuery,
                        searchResults = diarySearchResults,
                        onSearchQueryChange = { diarySearchQuery = it },
                        onSearchResultSelected = { result ->
                            selectedDateValue = result.date.toString()
                            diarySearchQuery = ""
                        },
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.Summary.route -> SummaryScreen(
                        tree = com.cypress.diary.ui.summary.SummaryTreeBuilder().build(activeDocuments),
                        selectedDocument = selectedSummaryDocument,
                        refreshing = refreshing,
                        onRefresh = { refreshWeeks() },
                        onDocumentSelected = { document -> selectedSummaryPath = document.path },
                        onDocumentDismiss = { selectedSummaryPath = null },
                        onEditDocument = { document ->
                            editorDocumentPath = document.path
                            route = DiaryRoute.Editor.route
                        },
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.Profile.route -> ProfileScreen(
                        selectedPalette = palette,
                        onPaletteSelected = {
                            persistAppearance(updatedPalette = it)
                        },
                        githubConfig = githubConfig,
                        connectionStatus = connectionStatus,
                        backgroundUri = appearance.backgroundUri,
                        refreshing = refreshing,
                        onRefresh = { refreshWeeks() },
                        onGitHubConnect = { config ->
                            val normalized = config.normalized()
                            configStore.save(normalized)
                            githubConfig = normalized
                        },
                        onBackgroundSelected = { uri ->
                            persistAppearance(backgroundUri = uri)
                        },
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.Editor.route -> EditorScreen(
                        date = selectedDate,
                        mode = editorMode,
                        draft = editorDraft,
                        refreshing = refreshing,
                        onRefresh = { refreshWeeks() },
                        onBack = {
                            route = if (editorDocumentPath != null) DiaryRoute.Summary.route else DiaryRoute.Diary.route
                            editorDocumentPath = null
                        },
                        onModeChange = { nextMode ->
                            editorModeName = nextMode.name
                            editorDraft = loadInitialEditorDraft(
                                builder = editorBuilder,
                                draftStore = draftStore,
                                editorPath = editorPath,
                                date = selectedDate,
                                week = selectedWeek,
                                mode = nextMode,
                            )
                        },
                        onDraftChange = { nextDraft ->
                            editorDraft = nextDraft
                            draftStore.save(editorKey, nextDraft)
                            draftSnapshots = updateDraftSnapshot(draftSnapshots, editorKey, nextDraft)
                        },
                        onPush = {
                            scope.launch {
                                runCatching {
                                    val config = githubConfig ?: error("请先连接 GitHub")
                                    val path = editorDocumentPath ?: editorPath
                                    val markdown = if (editorDocumentPath != null) {
                                        editorDraft
                                    } else {
                                        buildEditorMarkdown(
                                            builder = editorBuilder,
                                            date = selectedDate,
                                            week = selectedWeek,
                                            mode = editorMode,
                                            draft = editorDraft,
                                        )
                                    }
                                    repository.saveWeek(config, path, markdown)
                                    draftStore.clear(editorKey)
                                    draftSnapshots = clearDraftSnapshot(draftSnapshots, editorKey)
                                    Toast.makeText(context, "推送成功了", Toast.LENGTH_SHORT).show()
                                }.onFailure { error ->
                                    connectionStatus = "推送失败：${error.message ?: "未知错误"}"
                                    Toast.makeText(context, "推送失败了", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                        title = if (editorDocumentPath != null) "编辑总结" else "编辑日记",
                        pathText = editorDocumentPath?.let { "路径：$it" }
                            ?: "路径：${selectedDate.year} / ${selectedDate.monthValue} / ${selectedDate.dayOfMonth}",
                        showModeSelector = editorDocumentPath == null,
                        contentLabel = if (editorDocumentPath != null) "Markdown 原文" else if (editorMode == EditMode.Day) "当天内容" else "整周 Markdown",
                    )
                }
            }
        }
    }
}

private fun documentsToWeeks(
    documents: List<DiaryDocument>,
    codec: DiaryMarkdownCodec,
): List<DiaryWeek> {
    return documents
        .filter { it.type == DiaryDocumentType.Week }
        .mapNotNull { document -> runCatching { codec.parse(document.markdown) }.getOrNull() }
        .sortedWith(compareBy({ it.key.year }, { it.key.month }, { it.key.weekIndex }))
}

private fun resolvePalette(name: String): ThemePalette {
    return ThemePalette.values().firstOrNull { it.name == name } ?: ThemePalette.BlueGray
}

internal fun shouldFetchRemoteDiary(refreshVersion: Int): Boolean {
    return refreshVersion > 0
}

internal fun updateDraftSnapshot(
    drafts: Map<String, String>,
    key: String,
    value: String,
): Map<String, String> {
    return drafts + (key to value)
}

internal fun clearDraftSnapshot(
    drafts: Map<String, String>,
    key: String,
): Map<String, String> {
    return drafts - key
}

private fun loadInitialEditorDraft(
    builder: DiaryEditContentBuilder,
    draftStore: EditorDraftStore,
    editorPath: String,
    date: LocalDate,
    week: DiaryWeek?,
    mode: EditMode,
): String {
    val key = editorDraftKey(editorPath, mode, date)
    val saved = draftStore.load(key)
    if (!saved.isNullOrBlank()) return saved

    return when (mode) {
        EditMode.Day -> week?.days?.firstOrNull { it.date == date }?.content.orEmpty()
        EditMode.Week -> week?.let(builder::render) ?: builder.render(builder.newWeek(date))
    }
}

private fun buildEditorMarkdown(
    builder: DiaryEditContentBuilder,
    date: LocalDate,
    week: DiaryWeek?,
    mode: EditMode,
    draft: String,
): String {
    return when (mode) {
        EditMode.Day -> {
            val baseWeek = week ?: builder.newWeek(date)
            builder.render(builder.updateDayContent(baseWeek, date, draft))
        }
        EditMode.Week -> if (draft.isBlank()) {
            week?.let(builder::render) ?: builder.render(builder.newWeek(date))
        } else {
            draft
        }
    }
}

private fun editorDraftKey(editorPath: String, mode: EditMode, date: LocalDate): String {
    return when (mode) {
        EditMode.Day -> "$editorPath#day-${date}"
        EditMode.Week -> "$editorPath#week"
    }
}

private fun documentDraftKey(path: String): String = "document:$path"

private fun List<DiaryWeek>.findByDate(date: LocalDate): DiaryWeek? {
    return firstOrNull { week -> week.days.any { it.date == date } }
        ?: firstOrNull { it.key == WeekKey.from(date) }
        ?: maxByOrNull { it.published }
}
