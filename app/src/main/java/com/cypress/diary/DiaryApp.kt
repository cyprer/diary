package com.cypress.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.net.Uri
import android.widget.Toast
import com.cypress.diary.accounting.mergeAccountingRecords
import com.cypress.diary.accounting.replaceAccountingRecords
import com.cypress.diary.export.AccountingExportArchive
import com.cypress.diary.export.DiaryExportArchive
import com.cypress.diary.github.GitHubConfig
import com.cypress.diary.github.GitHubConfigStore
import com.cypress.diary.github.GitHubDiaryRepository
import com.cypress.diary.model.DiaryDocument
import com.cypress.diary.model.DiaryDocumentType
import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import com.cypress.diary.model.accounting.AccountingRecord
import com.cypress.diary.parser.DiaryDocumentCodec
import com.cypress.diary.parser.DiaryMarkdownCodec
import com.cypress.diary.parser.WeekPathResolver
import com.cypress.diary.quote.DailyQuoteRepository
import com.cypress.diary.storage.AccountingRecordStore
import com.cypress.diary.storage.AppAppearanceStore
import com.cypress.diary.storage.AppModuleStore
import com.cypress.diary.storage.DailyQuoteStore
import com.cypress.diary.storage.DiaryDocumentCacheStore
import com.cypress.diary.storage.DiaryWeekCacheStore
import com.cypress.diary.storage.EditorDraftStore
import com.cypress.diary.storage.SharedPreferencesPreferenceStore
import com.cypress.diary.ui.components.AppBackground
import com.cypress.diary.ui.navigation.AppModule
import com.cypress.diary.ui.navigation.DiaryRoute
import com.cypress.diary.ui.sample.sampleDiaryWeeks
import com.cypress.diary.ui.screens.DiaryScreen
import com.cypress.diary.ui.screens.EditorScreen
import com.cypress.diary.ui.screens.EditMode
import com.cypress.diary.ui.screens.AccountingEditorScreen
import com.cypress.diary.ui.screens.AccountingLedgerScreen
import com.cypress.diary.ui.screens.AccountingStatsScreen
import com.cypress.diary.ui.screens.AccountingStatsMode
import com.cypress.diary.ui.screens.ProfileScreen
import com.cypress.diary.ui.screens.SummaryScreen
import com.cypress.diary.ui.calendar.DiaryCalendarMode
import com.cypress.diary.ui.editor.DraftContentResolver
import com.cypress.diary.ui.editor.DiaryEditContentBuilder
import com.cypress.diary.ui.editor.withSummaryBody
import com.cypress.diary.ui.search.searchDiaryWeeks
import com.cypress.diary.ui.summary.monthLocalWeekDates
import com.cypress.diary.ui.summary.weekSummaryDays
import com.cypress.diary.ui.theme.DiaryTheme
import com.cypress.diary.ui.theme.ThemePalette
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DiaryApp() {
    val context = LocalContext.current.applicationContext
    val configStore = remember(context) { GitHubConfigStore(context) }
    val appearanceStore = remember(context) {
        AppAppearanceStore(context.getSharedPreferences("app_appearance", android.content.Context.MODE_PRIVATE))
    }
    val moduleStore = remember(context) {
        AppModuleStore(context.getSharedPreferences("app_module", android.content.Context.MODE_PRIVATE))
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
    val accountingRecordStore = remember(context) {
        AccountingRecordStore(context.getSharedPreferences("accounting_records", android.content.Context.MODE_PRIVATE))
    }
    val repository = remember { GitHubDiaryRepository() }
    val quoteRepository = remember { DailyQuoteRepository() }
    val sampleWeeks = remember { sampleDiaryWeeks() }
    val editorBuilder = remember { DiaryEditContentBuilder() }
    val draftContentResolver = remember { DraftContentResolver() }
    val documentCodec = remember { DiaryDocumentCodec() }
    val weekCodec = remember { DiaryMarkdownCodec() }
    val weekPathResolver = remember { WeekPathResolver() }
    val exportArchive = remember { DiaryExportArchive() }
    val accountingExportArchive = remember { AccountingExportArchive() }
    val scope = rememberCoroutineScope()
    val initialModule = remember(moduleStore) { moduleStore.load() }

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
    var activeModuleName by rememberSaveable { mutableStateOf(initialModule.name) }
    var route by rememberSaveable {
        mutableStateOf(
            if (initialModule == AppModule.Accounting) {
                DiaryRoute.Ledger.route
            } else {
                DiaryRoute.Diary.route
            },
        )
    }
    var editorModeName by rememberSaveable { mutableStateOf(EditMode.Day.name) }
    var selectedSummaryPath by rememberSaveable { mutableStateOf<String?>(null) }
    var editorDocumentPath by rememberSaveable { mutableStateOf<String?>(null) }
    var dailyQuote by rememberSaveable { mutableStateOf<String?>(null) }
    var diarySearchQuery by rememberSaveable { mutableStateOf("") }
    var diaryCalendarModeName by rememberSaveable { mutableStateOf(DiaryCalendarMode.Month.name) }
    var selectedSummaryFallbackDocument by remember { mutableStateOf<DiaryDocument?>(null) }
    var editorDocumentFallback by remember { mutableStateOf<DiaryDocument?>(null) }
    var profileHiddenTapCount by rememberSaveable { mutableStateOf(0) }
    var profileHiddenLastTapAt by rememberSaveable { mutableStateOf(0L) }
    var githubSettingsRevealSignal by rememberSaveable { mutableStateOf(0) }
    var accountingRecords by remember { mutableStateOf(accountingRecordStore.loadRecords()) }
    var accountingMonthValue by rememberSaveable { mutableStateOf(YearMonth.now().toString()) }
    var accountingYearValue by rememberSaveable { mutableStateOf(YearMonth.now().year) }
    var accountingStatsModeName by rememberSaveable { mutableStateOf(AccountingStatsMode.Month.name) }
    var selectedAccountingRecordId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingAccountingImportRecords by remember { mutableStateOf<List<AccountingRecord>?>(null) }

    val selectedDate = LocalDate.parse(selectedDateValue)
    val activeModule = AppModule.valueOf(activeModuleName)
    val accountingMonth = YearMonth.parse(accountingMonthValue)
    val accountingStatsMode = AccountingStatsMode.valueOf(accountingStatsModeName)
    val selectedAccountingRecord = accountingRecords.firstOrNull { it.id == selectedAccountingRecordId }
    val sampleDocuments = remember(sampleWeeks) {
        sampleWeeks.mapNotNull { week ->
            val path = weekPathResolver.resolve(week.key)
            runCatching { documentCodec.parse(path, editorBuilder.render(week)) }.getOrNull()
        }
    }
    val activeDocuments = remoteDocuments ?: cachedDocuments ?: sampleDocuments
    val exportSourceDocuments = remoteDocuments ?: cachedDocuments ?: emptyList()
    val activeWeeks = remember(activeDocuments, cachedWeeks, sampleWeeks) {
        documentsToWeeks(activeDocuments, weekCodec).ifEmpty { cachedWeeks ?: sampleWeeks }
    }
    val selectedWeek = remember(activeWeeks, selectedDateValue) { activeWeeks.findByDate(selectedDate) }
    val palette = resolvePalette(appearance.paletteName)
    val requestedEditorMode = EditMode.valueOf(editorModeName)
    val editorMode = if (editorDocumentPath == null) EditMode.Day else requestedEditorMode
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
    val selectedSummaryBaseDocument = remember(activeDocuments, selectedSummaryPath, selectedSummaryFallbackDocument) {
        selectedSummaryPath?.let { path ->
            activeDocuments.firstOrNull { it.path == path }
                ?: selectedSummaryFallbackDocument?.takeIf { it.path == path }
        }
    }
    val selectedSummaryDocument = draftContentResolver.resolveDocument(
        document = selectedSummaryBaseDocument,
        localDraft = selectedSummaryBaseDocument?.let { document ->
            val key = documentDraftKey(document.path)
            draftSnapshots[key] ?: draftStore.load(key)
        },
    )
    val summaryEditorDocument = remember(activeDocuments, editorDocumentPath, editorDocumentFallback) {
        editorDocumentPath?.let { path ->
            activeDocuments.firstOrNull { it.path == path }
                ?: editorDocumentFallback?.takeIf { it.path == path }
        }
    }
    val summaryEditorWeek = remember(summaryEditorDocument) {
        summaryEditorDocument
            ?.takeIf { it.type == DiaryDocumentType.Week }
            ?.let { document -> runCatching { weekCodec.parse(document.markdown) }.getOrNull() }
    }
    val editorKey = remember(selectedDateValue, editorModeName, editorDocumentPath) {
        editorDocumentPath?.let(::documentDraftKey) ?: editorDraftKey(editorPath, editorMode, selectedDate)
    }
    var editorWeekDayDrafts by rememberSaveable(editorKey) {
        mutableStateOf(initialWeekSummaryDayDrafts(summaryEditorDocument, summaryEditorWeek))
    }
    var editorDraft by rememberSaveable(editorKey) {
        mutableStateOf(
            if (editorDocumentPath != null) {
                draftStore.load(editorKey) ?: summaryEditorDocument?.body.orEmpty()
            } else {
                loadInitialEditorDraft(editorBuilder, draftStore, editorPath, selectedDate, selectedWeek, editorMode)
            },
        )
    }

    fun persistAppearance(
        updatedPalette: ThemePalette = palette,
        backgroundUri: String? = appearance.backgroundUri,
        layoutOpacity: Float = appearance.layoutOpacity,
    ) {
        val next = appearance.copy(
            paletteName = updatedPalette.name,
            backgroundUri = backgroundUri,
            layoutOpacity = layoutOpacity,
        )
        appearanceStore.save(next.paletteName, next.backgroundUri, next.layoutOpacity)
        appearance = next
    }

    fun refreshWeeks() {
        refreshVersion += 1
    }

    fun selectModule(module: AppModule) {
        activeModuleName = module.name
        moduleStore.save(module)
        selectedAccountingRecordId = null
        route = when (module) {
            AppModule.Diary -> DiaryRoute.Diary.route
            AppModule.Accounting -> DiaryRoute.Ledger.route
        }
    }

    fun saveAccountingRecord(record: AccountingRecord) {
        accountingRecordStore.upsert(record)
        accountingRecords = accountingRecordStore.loadRecords()
    }

    fun deleteAccountingRecord(id: String) {
        accountingRecordStore.delete(id)
        accountingRecords = accountingRecordStore.loadRecords()
    }

    fun revealGitHubSettingsFromProfileTab() {
        val now = System.currentTimeMillis()
        profileHiddenTapCount = if (now - profileHiddenLastTapAt <= 1_000L) {
            profileHiddenTapCount + 1
        } else {
            1
        }
        profileHiddenLastTapAt = now
        if (profileHiddenTapCount >= 7) {
            profileHiddenTapCount = 0
            githubSettingsRevealSignal += 1
        }
    }

    fun exportDiary(uri: Uri) {
        scope.launch {
            val documents = documentsWithDraftsForExport(
                documents = exportSourceDocuments,
                drafts = draftStore.loadAll() + draftSnapshots,
                codec = weekCodec,
            )
            if (documents.isEmpty()) {
                Toast.makeText(context, "没有可导出的日记数据", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: error("无法打开导出文件")
                    output.use { stream ->
                        exportArchive.write(documents, stream)
                    }
                }
            }.onSuccess {
                Toast.makeText(context, "日记数据已导出", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(context, "导出失败：${error.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importDiary(uri: Uri) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error("无法打开导入文件")
                    input.use { stream ->
                        exportArchive.read(stream)
                    }
                }
            }.onSuccess { documents ->
                if (documents.isEmpty()) {
                    Toast.makeText(context, "没有读取到日记数据", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                val weeks = documentsToWeeks(documents, weekCodec)
                remoteDocuments = null
                cachedDocuments = documents
                cachedWeeks = weeks
                documentCacheStore.saveDocuments(documents)
                weekCacheStore.saveWeeks(weeks)
                connectionStatus = "已导入 ${documents.size} 个文档"
                Toast.makeText(context, "日记数据已导入", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(context, "导入失败：${error.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun exportAccounting(uri: Uri) {
        scope.launch {
            if (accountingRecords.isEmpty()) {
                Toast.makeText(context, "没有可导出的账单数据", Toast.LENGTH_SHORT).show()
                return@launch
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: error("无法打开导出文件")
                    output.use { stream ->
                        accountingExportArchive.write(accountingRecords, stream)
                    }
                }
            }.onSuccess {
                Toast.makeText(context, "账单数据已导出", Toast.LENGTH_SHORT).show()
            }.onFailure { error ->
                Toast.makeText(context, "导出失败：${error.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun importAccounting(uri: Uri) {
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: error("无法打开导入文件")
                    input.use { stream ->
                        accountingExportArchive.read(stream)
                    }
                }
            }.onSuccess { records ->
                if (records.isEmpty()) {
                    Toast.makeText(context, "没有读取到账单数据", Toast.LENGTH_SHORT).show()
                    return@onSuccess
                }
                pendingAccountingImportRecords = records
            }.onFailure { error ->
                Toast.makeText(context, "导入失败：${error.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun applyAccountingImport(replace: Boolean) {
        val imported = pendingAccountingImportRecords ?: return
        val nextRecords = if (replace) {
            replaceAccountingRecords(imported)
        } else {
            mergeAccountingRecords(accountingRecords, imported)
        }
        accountingRecordStore.saveRecords(nextRecords)
        accountingRecords = accountingRecordStore.loadRecords()
        pendingAccountingImportRecords = null
        Toast.makeText(
            context,
            if (replace) "账单数据已替换" else "账单数据已合并",
            Toast.LENGTH_SHORT,
        ).show()
    }

    fun saveDocumentLocally(document: DiaryDocument) {
        val nextDocuments = upsertDiaryDocument(
            documents = remoteDocuments ?: cachedDocuments ?: emptyList(),
            document = document,
        )
        val nextWeeks = documentsToWeeks(nextDocuments, weekCodec)
        remoteDocuments = null
        cachedDocuments = nextDocuments
        cachedWeeks = nextWeeks
        documentCacheStore.saveDocuments(nextDocuments)
        weekCacheStore.saveWeeks(nextWeeks)
    }

    fun buildCurrentEditorMarkdown(): Pair<String, String> {
        val path = editorDocumentPath ?: editorPath
        val markdown = if (editorDocumentPath != null) {
            if (summaryEditorDocument?.type == DiaryDocumentType.Week) {
                buildWeekSummaryMarkdown(
                    document = summaryEditorDocument,
                    summaryBody = editorDraft,
                    dayDrafts = editorWeekDayDrafts,
                    codec = weekCodec,
                )
            } else {
                summaryEditorDocument?.withSummaryBody(editorDraft)?.markdown ?: editorDraft
            }
        } else {
            buildEditorMarkdown(
                builder = editorBuilder,
                date = selectedDate,
                week = selectedWeek,
                mode = editorMode,
                draft = editorDraft,
            )
        }
        return path to markdown
    }

    fun saveCurrentEditorLocally(): Pair<String, String> {
        val (path, markdown) = buildCurrentEditorMarkdown()
        val document = documentCodec.parse(path, markdown)
        saveDocumentLocally(document)
        draftStore.clear(editorKey)
        draftSnapshots = clearDraftSnapshot(draftSnapshots, editorKey)
        return path to markdown
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
        AppBackground(
            backgroundUri = appearance.backgroundUri,
            layoutOpacity = appearance.layoutOpacity,
        ) {
            Scaffold(
                modifier = Modifier.alpha(appearance.layoutOpacity),
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar {
                        DiaryRoute.rootRoutesFor(activeModule).forEach { rootRoute ->
                            NavigationBarItem(
                                selected = isBottomRouteSelected(rootRoute, route),
                                onClick = { route = rootRoute.route },
                                icon = {
                                    Icon(
                                        imageVector = rootRoute.icon,
                                        contentDescription = rootRoute.label,
                                    )
                                },
                                label = { Text(rootRoute.label) },
                            )
                        }
                    }
                },
                floatingActionButton = {
                    when {
                        activeModule == AppModule.Diary && route == DiaryRoute.Diary.route -> {
                            FloatingActionButton(
                                onClick = {
                                    editorDocumentPath = null
                                    editorDocumentFallback = null
                                    editorModeName = EditMode.Day.name
                                    route = DiaryRoute.Editor.route
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "编辑",
                                )
                            }
                        }
                        activeModule == AppModule.Accounting && route == DiaryRoute.Ledger.route -> {
                            FloatingActionButton(
                                onClick = {
                                    selectedAccountingRecordId = null
                                    route = DiaryRoute.AccountingEditor.route
                                },
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "记一笔",
                                )
                            }
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
                        onDocumentSelected = { document ->
                            selectedSummaryPath = document.path
                            selectedSummaryFallbackDocument = document
                        },
                        onDocumentDismiss = {
                            selectedSummaryPath = null
                            selectedSummaryFallbackDocument = null
                        },
                        onEditDocument = { document ->
                            editorDocumentPath = document.path
                            editorDocumentFallback = document
                            route = DiaryRoute.Editor.route
                        },
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.Profile.route -> ProfileScreen(
                        currentModule = activeModule,
                        onModuleSelected = ::selectModule,
                        selectedPalette = palette,
                        onPaletteSelected = {
                            persistAppearance(updatedPalette = it)
                        },
                        githubConfig = githubConfig,
                        connectionStatus = connectionStatus,
                        githubSettingsRevealSignal = githubSettingsRevealSignal,
                        backgroundUri = appearance.backgroundUri,
                        layoutOpacity = appearance.layoutOpacity,
                        refreshing = refreshing,
                        onRefresh = { refreshWeeks() },
                        onGitHubConnect = { config ->
                            val normalized = config.normalized()
                            configStore.save(normalized)
                            githubConfig = normalized
                        },
                        onGitHubDisconnect = {
                            configStore.clear()
                            githubConfig = null
                            remoteDocuments = null
                            connectionStatus = "未连接"
                            Toast.makeText(context, "已退出 GitHub 连接", Toast.LENGTH_SHORT).show()
                        },
                        onExportDiary = ::exportDiary,
                        onImportDiary = ::importDiary,
                        onExportAccounting = ::exportAccounting,
                        onImportAccounting = ::importAccounting,
                        onBackgroundSelected = { uri ->
                            persistAppearance(backgroundUri = uri)
                        },
                        onLayoutOpacityChange = { opacity ->
                            persistAppearance(layoutOpacity = opacity)
                        },
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.Ledger.route -> AccountingLedgerScreen(
                        records = accountingRecords,
                        selectedMonth = accountingMonth,
                        onMonthChange = { month -> accountingMonthValue = month.toString() },
                        onRecordSelected = { record ->
                            selectedAccountingRecordId = record.id
                            route = DiaryRoute.AccountingEditor.route
                        },
                        refreshing = false,
                        onRefresh = {},
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.AccountingStats.route -> AccountingStatsScreen(
                        records = accountingRecords,
                        selectedMonth = accountingMonth,
                        onMonthChange = { month -> accountingMonthValue = month.toString() },
                        selectedYear = accountingYearValue,
                        onYearChange = { year -> accountingYearValue = year },
                        mode = accountingStatsMode,
                        onModeChange = { mode -> accountingStatsModeName = mode.name },
                        refreshing = false,
                        onRefresh = {},
                        modifier = Modifier.padding(innerPadding),
                    )

                    DiaryRoute.AccountingEditor.route -> AccountingEditorScreen(
                        record = selectedAccountingRecord,
                        refreshing = false,
                        onRefresh = {},
                        onBack = {
                            selectedAccountingRecordId = null
                            route = DiaryRoute.Ledger.route
                        },
                        onSave = { record ->
                            saveAccountingRecord(record)
                            selectedAccountingRecordId = null
                            route = DiaryRoute.Ledger.route
                            Toast.makeText(context, "账目已保存", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { id ->
                            deleteAccountingRecord(id)
                            selectedAccountingRecordId = null
                            route = DiaryRoute.Ledger.route
                            Toast.makeText(context, "账目已删除", Toast.LENGTH_SHORT).show()
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
                            editorDocumentFallback = null
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
                        weekDayDrafts = editorWeekDayDrafts.entries
                            .mapNotNull { (date, content) ->
                                runCatching { LocalDate.parse(date) to content }.getOrNull()
                            }
                            .sortedBy { it.first },
                        onWeekDayDraftChange = { date, content ->
                            editorWeekDayDrafts = editorWeekDayDrafts + (date.toString() to content)
                        },
                        onSave = {
                            scope.launch {
                                runCatching {
                                    saveCurrentEditorLocally()
                                    Toast.makeText(context, "已保存到本地", Toast.LENGTH_SHORT).show()
                                    route = if (editorDocumentPath != null) DiaryRoute.Summary.route else DiaryRoute.Diary.route
                                    editorDocumentPath = null
                                    editorDocumentFallback = null
                                }.onFailure { error ->
                                    Toast.makeText(context, "保存失败：${error.message ?: "未知错误"}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onPush = {
                            scope.launch {
                                runCatching {
                                    val config = githubConfig
                                        ?: error("请先连接 GitHub")
                                    val (path, markdown) = saveCurrentEditorLocally()
                                    repository.saveWeek(config, path, markdown)
                                    Toast.makeText(context, "推送成功", Toast.LENGTH_SHORT).show()
                                    route = if (editorDocumentPath != null) DiaryRoute.Summary.route else DiaryRoute.Diary.route
                                    editorDocumentPath = null
                                    editorDocumentFallback = null
                                }.onFailure { error ->
                                    connectionStatus = "推送失败：${error.message ?: "未知错误"}"
                                    Toast.makeText(context, "推送失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.padding(innerPadding),
                        title = if (editorDocumentPath != null) "编辑总结" else "编辑日记",
                        pathText = editorDocumentPath?.let { "路径：$it" }
                            ?: "路径：${selectedDate.year} / ${selectedDate.monthValue} / ${selectedDate.dayOfMonth}",
                        showModeSelector = false,
                        showPushButton = githubConfig != null,
                        contentLabel = if (editorDocumentPath != null) "总结内容" else if (editorMode == EditMode.Day) "当天内容" else "整周 Markdown",
                    )
                }
            }
            pendingAccountingImportRecords?.let { records ->
                AlertDialog(
                    onDismissRequest = { pendingAccountingImportRecords = null },
                    title = { Text("导入账单数据") },
                    text = { Text("读取到 ${records.size} 条账单。请选择导入方式。") },
                    confirmButton = {
                        TextButton(onClick = { applyAccountingImport(replace = false) }) {
                            Text("合并到账单")
                        }
                    },
                    dismissButton = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(onClick = { applyAccountingImport(replace = true) }) {
                                Text("替换本地账单")
                            }
                            TextButton(onClick = { pendingAccountingImportRecords = null }) {
                                Text("取消")
                            }
                        }
                    },
                )
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
        .mapNotNull { document ->
            runCatching { codec.parse(document.markdown) }
                .getOrNull()
                ?.let { week ->
                    val days = weekSummaryDays(document)
                    week.copy(
                        key = WeekKey(document.year, requireNotNull(document.month), requireNotNull(document.weekIndex)),
                        published = days.firstOrNull()?.date ?: week.published,
                        days = days,
                    )
                }
        }
        .sortedWith(compareBy({ it.key.year }, { it.key.month }, { it.key.weekIndex }))
}

private fun resolvePalette(name: String): ThemePalette {
    return ThemePalette.values().firstOrNull { it.name == name } ?: ThemePalette.BlueGray
}

internal fun shouldFetchRemoteDiary(refreshVersion: Int): Boolean {
    return refreshVersion > 0
}

internal fun isBottomRouteSelected(rootRoute: DiaryRoute?, route: String): Boolean {
    return when (rootRoute) {
        null -> false
        DiaryRoute.Diary -> route == DiaryRoute.Diary.route || route == DiaryRoute.Editor.route
        DiaryRoute.Ledger -> route == DiaryRoute.Ledger.route || route == DiaryRoute.AccountingEditor.route
        else -> route == rootRoute.route
    }
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

internal fun documentsWithDraftsForExport(
    documents: List<DiaryDocument>,
    drafts: Map<String, String>,
    codec: DiaryMarkdownCodec,
): List<DiaryDocument> {
    return documents.map { document ->
        val wholeDocumentDraft = drafts[documentDraftKey(document.path)]
        val weekDraft = drafts[editorDraftKey(document.path, EditMode.Week, document.published)]
        val documentWithBodyDraft = wholeDocumentDraft?.let(document::withSummaryBody)
        val baseDocument = documentWithBodyDraft ?: document
        val baseMarkdown = weekDraft ?: baseDocument.markdown
        val markdown = if (documentWithBodyDraft == null && document.type == DiaryDocumentType.Week) {
            applyDayDraftsToWeekMarkdown(document.path, baseMarkdown, drafts, codec)
        } else {
            baseMarkdown
        }
        baseDocument.copy(markdown = markdown)
    }
}

private fun applyDayDraftsToWeekMarkdown(
    path: String,
    markdown: String,
    drafts: Map<String, String>,
    codec: DiaryMarkdownCodec,
): String {
    val week = runCatching { codec.parse(markdown) }.getOrNull() ?: return markdown
    val dayDraftPrefix = "$path#day-"
    val dayDrafts = drafts.mapNotNull { (key, value) ->
        if (!key.startsWith(dayDraftPrefix)) return@mapNotNull null
        runCatching { LocalDate.parse(key.removePrefix(dayDraftPrefix)) to value }.getOrNull()
    }.toMap()
    if (dayDrafts.isEmpty()) return markdown

    val existingDays = week.days.associateBy { it.date }
    val dates = (existingDays.keys + dayDrafts.keys).sorted()
    val updatedDays = dates.map { date ->
        DiaryDay(
            date = date,
            content = dayDrafts[date] ?: existingDays[date]?.content.orEmpty(),
        )
    }
    return codec.render(week.copy(days = updatedDays))
}

internal fun upsertDiaryDocument(
    documents: List<DiaryDocument>,
    document: DiaryDocument,
): List<DiaryDocument> {
    return (documents.filterNot { it.path == document.path } + document)
        .sortedWith(compareBy({ it.year }, { it.month ?: 0 }, { it.type.ordinal }, { it.weekIndex ?: 0 }))
}

private fun initialWeekSummaryDayDrafts(
    document: DiaryDocument?,
    week: DiaryWeek?,
): Map<String, String> {
    if (document?.type != DiaryDocumentType.Week) return emptyMap()
    val existingDays = week?.days.orEmpty().associateBy { it.date }
    return monthLocalWeekDates(document).associate { date ->
        date.toString() to existingDays[date]?.content.orEmpty()
    }
}

private fun buildWeekSummaryMarkdown(
    document: DiaryDocument,
    summaryBody: String,
    dayDrafts: Map<String, String>,
    codec: DiaryMarkdownCodec,
): String {
    val weekKey = WeekKey(document.year, requireNotNull(document.month), requireNotNull(document.weekIndex))
    val baseWeek = runCatching { codec.parse(document.markdown) }.getOrElse {
        DiaryWeek(
            key = weekKey,
            title = document.title,
            intro = document.body,
            published = document.published,
            description = document.title,
            tags = emptyList(),
            category = "周报",
            draft = false,
            days = emptyList(),
        )
    }
    val existingDays = baseWeek.days.associateBy { it.date }
    val dates = monthLocalWeekDates(document)
    val days = dates.map { date ->
        DiaryDay(
            date = date,
            content = dayDrafts[date.toString()] ?: existingDays[date]?.content.orEmpty(),
        )
    }
    return codec.render(
        baseWeek.copy(
            key = weekKey,
            published = dates.firstOrNull() ?: document.published,
            intro = summaryBody,
            days = days,
        ),
    )
}

private fun monthLocalWeekDates(document: DiaryDocument): List<LocalDate> {
    return monthLocalWeekDates(
        year = document.year,
        month = requireNotNull(document.month),
        weekIndex = requireNotNull(document.weekIndex),
    )
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

internal fun findDiaryWeekByDate(
    weeks: List<DiaryWeek>,
    date: LocalDate,
): DiaryWeek? {
    return weeks.firstOrNull { week -> week.days.any { it.date == date } }
        ?: weeks.firstOrNull { it.key == WeekKey.from(date) }
}

private fun List<DiaryWeek>.findByDate(date: LocalDate): DiaryWeek? {
    return findDiaryWeekByDate(this, date)
}
