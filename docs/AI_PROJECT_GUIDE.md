# Diary Android 项目 AI 接手指南

这份文档给后续接手本项目的 AI 或开发者使用。目标是先理解现有设计边界，再做扩展，避免把普通用户、本地数据、GitHub 隐藏同步和周/月/年总结逻辑混在一起。

## 项目定位

这是一个 Jetpack Compose Android 日记应用。

核心原则：

- 普通用户默认本地使用，不需要 GitHub，也不应该看到推送、仓库、Token 等概念。
- GitHub 是隐藏的个人同步能力，只在“我的”页头像 7 次快速点击后出现配置弹窗。
- GitHub 连接后，本地保存和远程推送是两个动作：保存只写本地，推送才写 GitHub。
- 总结页面展示年结、月结、周结；周结同时包含“这周总结”和“这周每天日记”。
- 不要把 Markdown 原文暴露给普通用户。编辑总结时编辑的是正文输入框，不是整份 Markdown。
- 应用现在包含日记和记账两个模块。模块切换入口在“我的”页；记账数据必须保持本地独立，不接入日记 Markdown、GitHub 同步或 `.diary` 导入导出。

## 技术栈和入口

- 语言：Kotlin
- UI：Jetpack Compose + Material 3
- 构建：Gradle
- Android 包名：`com.cypress.diary`
- 主入口：[MainActivity.kt](../app/src/main/java/com/cypress/diary/MainActivity.kt)
- Compose 应用入口：[DiaryApp.kt](../app/src/main/java/com/cypress/diary/DiaryApp.kt)

常用命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
C:\Users\cypress\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
```

如果需要清空手机本地数据：

```powershell
C:\Users\cypress\AppData\Local\Android\Sdk\platform-tools\adb.exe uninstall com.cypress.diary
C:\Users\cypress\AppData\Local\Android\Sdk\platform-tools\adb.exe install app\build\outputs\apk\debug\app-debug.apk
```

## 目录地图

重要目录：

- `model/`：核心数据模型，包含 `DiaryWeek`、`DiaryDay`、`DiaryDocument`、`WeekKey`。
- `model/accounting/`：记账记录模型，包含账目类型和单条账目。
- `accounting/`：记账金额解析、格式化、月度统计和分类汇总纯逻辑。
- `parser/`：Markdown 和路径解析。
- `github/`：GitHub 仓库配置、路径枚举、读取和推送。
- `storage/`：SharedPreferences 缓存、草稿、外观设置、模块选择和记账记录存储。
- `ui/navigation/`：应用路由和日记/记账模块定义。
- `ui/screens/`：主要页面，包含日记、总结、编辑、我的、账本、记账统计、记账编辑。
- `ui/calendar/`：年月周选择器。
- `ui/editor/`：编辑正文和草稿合并逻辑。
- `ui/summary/`：总结树和周结日记筛选逻辑。
- `export/`：`.diary` 导入导出文件。
- `app/src/test/`：单元测试，新增逻辑尽量先加这里的测试。

## 核心数据模型

`DiaryWeek` 是实际日记周数据：

- `key: WeekKey` 表示 year/month/weekIndex。
- `intro` 是周结正文。
- `days` 是每天日记列表。
- `published` 用于 Markdown front matter，但不要只依赖它判断周号。

`DiaryDocument` 是总结页面用的文档模型：

- `type` 可为 `Year`、`Month`、`Week`。
- `path` 是判断年/月/周位置的关键来源。
- `body` 是总结正文，不应该包含每天日记小节。
- `markdown` 是完整 Markdown，包括 front matter、标题、正文和日记小节。

关键提醒：周文档的真实周号以路径为准，例如：

```text
src/content/posts/summary/26year/5month/3week.md
```

不要只用 front matter 的 `published` 推导周号。旧数据或外部仓库文件可能把 `published` 写成月初日期。

## 日记数据流

普通用户本地保存：

1. 日记页选择日期。
2. 点击编辑进入 [EditorScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt)。
3. `DiaryApp.buildCurrentEditorMarkdown()` 生成 Markdown。
4. `DiaryDocumentCodec.parse(path, markdown)` 生成 `DiaryDocument`。
5. `saveDocumentLocally()` 写入 `DiaryDocumentCacheStore` 和 `DiaryWeekCacheStore`。

GitHub 用户：

1. 隐藏设置中输入 owner/repo/branch/token。
2. 下拉刷新才从 GitHub 拉取，不在启动时自动拉取。
3. 编辑页面有 `取消 / 保存 / 推送`。
4. 保存只写本地，推送会先保存本地，再调用 `GitHubDiaryRepository.saveWeek()`。

注意：普通用户界面不要出现推送按钮。

## 记账模块

模块入口：

- “我的”页展示日记 / 记账模块切换，见 [ProfileScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt)。
- 当前模块保存在 [AppModuleStore.kt](../app/src/main/java/com/cypress/diary/storage/AppModuleStore.kt)。
- 日记模块底部导航：`日记 / 总结 / 我的`。
- 记账模块底部导航：`账本 / 统计 / 我的`。

核心文件：

- [AppModule.kt](../app/src/main/java/com/cypress/diary/ui/navigation/AppModule.kt)
- [DiaryRoute.kt](../app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt)
- [AccountingRecord.kt](../app/src/main/java/com/cypress/diary/model/accounting/AccountingRecord.kt)
- [AccountingMoney.kt](../app/src/main/java/com/cypress/diary/accounting/AccountingMoney.kt)
- [AccountingSummary.kt](../app/src/main/java/com/cypress/diary/accounting/AccountingSummary.kt)
- [AccountingRecordStore.kt](../app/src/main/java/com/cypress/diary/storage/AccountingRecordStore.kt)
- [AccountingLedgerScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingLedgerScreen.kt)
- [AccountingStatsScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt)
- [AccountingEditorScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt)

行为边界：

- 记账只做本地账目管理，支持新增、编辑、删除、月度账本、月度统计和年度总结。
- 账目存储在单独的 `accounting_records` SharedPreferences 中，不写入 `DiaryDocumentCacheStore` 或 `DiaryWeekCacheStore`。
- 记账模块不触发 GitHub 拉取、推送或 `.diary` 导入导出。
- 记账页面下拉刷新参数固定为空操作，避免误触发日记远程刷新。
- 从记账编辑页返回时回到 `账本`，不要回到日记页。
- 如果上次保存的模块是记账，应用启动时默认进入 `账本`。
- 统计页支持 `月度 / 年度` 切换。月度统计看当前月，年度统计看全年概览、12 个月趋势、年度支出分类和年度收入分类。
- 账单导入导出使用独立 `.accounting` 文件，本质是 zip 包，入口在“我的”页数据区。
- `.accounting` 不等于 `.diary`：日记导入导出只处理日记，账单导入导出只处理账单。
- 导入账单时先读取文件，再让用户选择“替换本地账单”或“合并到账单”。合并按 `id` 去重，导入文件里的同 id 记录覆盖本地记录。
- 记账编辑页支持自定义分类。自定义分类按收入/支出区分，存储在 `AccountingCategoryStore`，并会随 `.accounting` 文件一起导出导入。
- 自定义分类导入时按 `type + label` 去重。替换导入会替换本地自定义分类，合并导入会保留本地独有分类。

历史 bug：

- `DiaryRoute` 的根路由列表不能在 companion 初始化时直接保存为 `val listOf(Diary, ...)`。嵌套 `data object` 可能还没完成初始化，运行时列表会出现 `null`，导致启动时底部导航闪退。
- 正确做法是把 `diaryRootRoutes`、`accountingRootRoutes`、`rootRoutes` 写成 getter，每次访问时再构造列表。
- 底部导航选中逻辑集中在 `isBottomRouteSelected()`，编辑页归到日记 tab，记账编辑页归到账本 tab，并且要能处理异常 null。

重点测试文件：

- `AppModuleStoreTest.kt`
- `DiaryRouteTest.kt`
- `DiaryAppNavigationTest.kt`
- `AccountingMoneyTest.kt`
- `AccountingSummaryTest.kt`
- `AccountingRecordStoreTest.kt`
- `AccountingExportArchiveTest.kt`
- `AccountingCategoryStoreTest.kt`

## 周逻辑的坑

本项目里“周”有两种含义，必须区分：

- 日历周：周日到周六，用在日记页周选择器。
- 月内第 N 周：每月 1-7 是第 1 周，8-14 是第 2 周，依此类推，用在文件路径和总结树。

相关文件：

- [WeekKey.kt](../app/src/main/java/com/cypress/diary/model/WeekKey.kt)
- [WeekPathResolver.kt](../app/src/main/java/com/cypress/diary/parser/WeekPathResolver.kt)
- [CalendarMonthPicker.kt](../app/src/main/java/com/cypress/diary/ui/calendar/CalendarMonthPicker.kt)
- [WeekSummaryContent.kt](../app/src/main/java/com/cypress/diary/ui/summary/WeekSummaryContent.kt)

历史 bug：

- 如果选中一个还没有日记的日期，旧逻辑会拿最后一个已有周兜底，导致后续周保存出第一周内容。
- 修复点是 `findDiaryWeekByDate()`：只能返回包含该日期的周，或 key 正好匹配该日期所在月内周的周；不能 fallback 到任意最后一周。

新增或修改周逻辑时，请跑这些测试：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
```

重点测试文件：

- `DiaryAppRefreshTest.kt`
- `CalendarMonthTest.kt`
- `DateSelectionMathTest.kt`
- `WeekSummaryContentTest.kt`
- `DiaryEditContentBuilderTest.kt`

## 总结页面

总结页面入口：[SummaryScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt)

行为：

- 年结和月结展示普通 Markdown 正文。
- 周结展示两部分：
  - 周结正文，也就是 Markdown 标题后的正文。
  - 本周每天日记，来自同一周 Markdown 里的 `## 月.日` 小节。

周结每天日记不能直接展示 Markdown 文件里的所有日记小节。必须通过 `weekSummaryDays(document)` 按 `DiaryDocument.path` 的 year/month/weekIndex 筛选这一周日期。

编辑总结：

- 点周结弹窗的编辑，会进入编辑页。
- 周结编辑页包含总结正文输入框和本周每天输入框。
- 月结、年结只编辑正文。
- 不要展示 front matter、`#`、`##` 等 Markdown 结构给用户。

相关文件：

- [SummaryDocumentBody.kt](../app/src/main/java/com/cypress/diary/ui/editor/SummaryDocumentBody.kt)
- [DraftContentResolver.kt](../app/src/main/java/com/cypress/diary/ui/editor/DraftContentResolver.kt)
- [DiaryDocumentCodec.kt](../app/src/main/java/com/cypress/diary/parser/DiaryDocumentCodec.kt)

## GitHub 同步设计

配置模型：

- [GitHubConfig.kt](../app/src/main/java/com/cypress/diary/github/GitHubConfig.kt)
- [GitHubConfigStore.kt](../app/src/main/java/com/cypress/diary/github/GitHubConfigStore.kt)

仓库访问：

- [GitHubDiaryRepository.kt](../app/src/main/java/com/cypress/diary/github/GitHubDiaryRepository.kt)
- [GitHubDiaryPaths.kt](../app/src/main/java/com/cypress/diary/github/GitHubDiaryPaths.kt)

规则：

- 不要写死 owner、repo、token。
- 默认配置必须为空。
- 隐藏入口是“我的”页头像 7 次快速点击。
- 底部导航的“我的”只做普通导航，不触发弹窗。
- 连接 GitHub 后，个人页左上角显示退出连接按钮。
- 退出连接只清除 GitHub 配置和 token，不删除本地日记。
- 下拉刷新才拉取 GitHub 数据。

普通用户相关要求：

- 不显示 GitHub 配置。
- 不显示推送按钮。
- 不需要知道 Markdown 或仓库路径。

## 导入导出

导入导出使用 `.diary` 文件，本质是 zip 包。

相关文件：

- [DiaryExportArchive.kt](../app/src/main/java/com/cypress/diary/export/DiaryExportArchive.kt)
- `DiaryExportArchiveTest.kt`

行为：

- 导出：把当前本地文档和未推送草稿合并后写成 `.diary` 文件。
- 导入：读取 `.diary` 文件并替换本地缓存。

普通用户主要依赖这个能力做手动迁移和同步。

## 外观和个人页

个人页入口：[ProfileScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt)

现有外观能力：

- 主题色。
- 背景图。
- 布局透明度。

存储位置：

- [AppAppearanceStore.kt](../app/src/main/java/com/cypress/diary/storage/AppAppearanceStore.kt)

背景图：

- [AppBackground.kt](../app/src/main/java/com/cypress/diary/ui/components/AppBackground.kt)

布局透明度会影响主 Scaffold 透明度和背景遮罩。调低透明度时，背景图更明显。

## 本地缓存和草稿

主要存储：

- `DiaryDocumentCacheStore`：总结文档缓存，保存完整 Markdown。
- `DiaryWeekCacheStore`：周数据缓存。
- `EditorDraftStore`：编辑草稿。
- `DailyQuoteStore`：每日一句缓存。
- `AppAppearanceStore`：主题、背景图、布局透明度。
- `AppModuleStore`：当前模块选择，默认日记。
- `AccountingRecordStore`：记账记录，独立于日记缓存和导入导出。

重要原则：

- 本地保存后要立刻更新内存状态和缓存，否则返回日记页不会立即显示。
- 草稿优先于远程或缓存内容。
- 推送成功后清除对应草稿。
- 退出 GitHub 不清除本地缓存。

## UI 约定

主要页面：

- 日记页：[DiaryScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt)
- 总结页：[SummaryScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt)
- 编辑页：[EditorScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt)
- 我的页：[ProfileScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt)
- 账本页：[AccountingLedgerScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingLedgerScreen.kt)
- 记账统计页：[AccountingStatsScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingStatsScreen.kt)
- 记账编辑页：[AccountingEditorScreen.kt](../app/src/main/java/com/cypress/diary/ui/screens/AccountingEditorScreen.kt)

约定：

- 普通用户路径优先，不要为了 GitHub 用户污染默认界面。
- 编辑日记时普通用户只能一天一天编辑。
- 编辑周结时才允许同时编辑一周每天日记。
- 弹窗标题不要重复。
- 总结弹窗支持上一个/下一个。
- 我的页标题顶部居中；GitHub 用户左上角有退出连接按钮。
- 模块切换放在“我的”页，不要在普通日记页面增加额外切换入口。
- 记账模块不要显示 GitHub、Markdown、仓库路径、`.diary` 等日记同步概念。

## 测试策略

修改行为前优先加单元测试。

推荐测试范围：

- Markdown 解析和渲染：`parser/`
- 周和日期选择：`ui/calendar/`、`ui/state/`
- 编辑和草稿合并：`ui/editor/`、`storage/`
- GitHub 路径和 payload：`github/`
- 导入导出：`export/`
- DiaryApp 级别纯函数：`DiaryAppRefreshTest.kt`
- 模块路由和底部导航：`DiaryRouteTest.kt`、`DiaryAppNavigationTest.kt`
- 记账金额、统计、存储：`AccountingMoneyTest.kt`、`AccountingSummaryTest.kt`、`AccountingRecordStoreTest.kt`

每次完成后至少跑：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## 扩展建议

新增功能时按这个顺序判断：

1. 这个能力是普通用户需要，还是隐藏 GitHub 用户需要？
2. 数据应该存在本地缓存、导出文件、GitHub Markdown，还是只存在界面状态？
3. 是否会影响周/月/年路径？
4. 是否会暴露 Markdown 或 GitHub 信息给普通用户？
5. 是否需要导入导出支持？
6. 是否需要测试旧数据兼容？

常见扩展点：

- 新增日记字段：先改模型和 codec，再改导入导出和编辑 UI。
- 新增同步方式：不要耦合到 GitHub 逻辑，优先抽象成独立 repository。
- 新增统计页：优先从 `DiaryDocument` 或 `DiaryWeek` 派生，不要直接读 UI 状态。
- 新增主题能力：扩展 `AppAppearanceStore` 并补测试。
- 新增记账能力：优先扩展 `accounting/` 纯逻辑和 `AccountingRecordStore`，不要耦合到日记 Markdown、GitHub 或 `.diary`。

## 提交和安装习惯

用户通常希望：

- 做完先构建 APK。
- 安装前先问，除非用户明确说安装。
- 如果用户说“卸载重装”，使用 uninstall + install，清空本地数据。
- 提交代码时不要把 `.idea/`、临时图片、无关计划文档一起提交。

提交前建议检查：

```powershell
git status --short
git diff --cached --stat
```

## 高风险改动清单

这些地方改动前要格外小心：

- `WeekKey.from()`：会影响路径和周缓存。
- `findDiaryWeekByDate()`：会影响普通用户保存新周。
- `DiaryMarkdownCodec.render()`：会影响 GitHub 文件格式。
- `DiaryDocumentCodec.parse()`：会影响总结树、导入导出和缓存恢复。
- `GitHubConfigStore`：不要引入默认私人仓库或 token。
- `ProfileScreen`：不要让普通用户看见 GitHub 能力。
- `DiaryDocumentCacheStore`：旧缓存兼容很重要。
- `DiaryRoute` companion 根路由：必须使用 getter，避免嵌套 `data object` 初始化顺序导致 null。
- `isBottomRouteSelected()`：影响底部导航选中状态和编辑页归属。
- `AccountingRecordStore`：记账数据格式需要兼容旧记录，损坏单行应跳过而不是让应用崩溃。
- `AppModuleStore`：异常模块值必须回退到日记模块。

如果不确定，先写一个最小失败测试，再改实现。
