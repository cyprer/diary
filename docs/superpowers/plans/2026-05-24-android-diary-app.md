# 安卓日记 App Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 做一个安卓日记 App，支持 GitHub 登录、阅读/编辑周记、自动创建新周记文件，并把修改直接同步回你的博客仓库。

**Architecture:** 采用纯客户端方案，不加后端。App 用 Jetpack Compose 负责界面，用 MVVM + Flow 管状态，用 GitHub Device Flow 完成登录，用 GitHub Contents API 读写 Markdown。周记文件作为唯一事实来源，App 只负责解析、编辑、重建和同步。

**Tech Stack:** Kotlin, Java 17, Jetpack Compose, Navigation-Compose, Material 3, Coroutines/Flow, Room, DataStore, Retrofit, OkHttp, MockWebServer, JUnit5/JUnit4, Compose UI Test.

---

## 文件边界

- `app/src/main/java/com/cypress/diary/MainActivity.kt`
  - 入口 Activity，只负责挂载 `DiaryApp`
- `app/src/main/java/com/cypress/diary/DiaryApp.kt`
  - 顶层导航和应用级状态入口
- `app/src/main/java/com/cypress/diary/model/*`
  - `DiaryWeek`、`DiaryDay`、`WeekKey` 这类领域模型
- `app/src/main/java/com/cypress/diary/parser/*`
  - Markdown 解析、重建、周记路径推导
- `app/src/main/java/com/cypress/diary/auth/*`
  - GitHub Device Flow 登录
- `app/src/main/java/com/cypress/diary/github/*`
  - GitHub Contents API 读写仓库文件
- `app/src/main/java/com/cypress/diary/storage/*`
  - Token 存储、Room 草稿库
- `app/src/main/java/com/cypress/diary/feature/*`
  - 日记页、总结页、我的页、编辑页的 ViewModel 和业务协调
- `app/src/main/java/com/cypress/diary/ui/*`
  - Compose 屏幕、组件、主题、底部导航
- `app/src/test/java/com/cypress/diary/*`
  - 单元测试
- `app/src/androidTest/java/com/cypress/diary/*`
  - Compose UI 测试

## Task 1: 创建 Android 工程骨架

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/cypress/diary/MainActivity.kt`
- Create: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/theme/Color.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/theme/Theme.kt`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: 写最小启动壳**
  - `MainActivity` 只做 `setContent { DiaryApp() }`
  - `DiaryApp` 先只渲染一个空的 Scaffold，底部导航留位置

- [ ] **Step 2: 跑一次构建**
  - Run: `./gradlew :app:assembleDebug`
  - Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 提交**
  - `git add settings.gradle.kts build.gradle.kts gradle.properties gradle/wrapper/gradle-wrapper.properties gradlew gradlew.bat app`
  - `git commit -m "feat: scaffold diary android app"`

## Task 2: 建立周记模型、Markdown 编解码和路径规则

**Files:**
- Create: `app/src/main/java/com/cypress/diary/model/WeekKey.kt`
- Create: `app/src/main/java/com/cypress/diary/model/DiaryWeek.kt`
- Create: `app/src/main/java/com/cypress/diary/model/DiaryDay.kt`
- Create: `app/src/main/java/com/cypress/diary/parser/WeekPathResolver.kt`
- Create: `app/src/main/java/com/cypress/diary/parser/DiaryMarkdownCodec.kt`
- Create: `app/src/test/java/com/cypress/diary/parser/WeekPathResolverTest.kt`
- Create: `app/src/test/java/com/cypress/diary/parser/DiaryMarkdownCodecTest.kt`

- [ ] **Step 1: 先写失败测试**
  ```kotlin
  @Test
  fun roundTrip_preserves_week_body() {
      val input = sampleMarkdown()
      val parsed = DiaryMarkdownCodec.parse(input)
      val output = DiaryMarkdownCodec.render(parsed)
      assertEquals(input.trim(), output.trim())
  }
  ```

  ```kotlin
  @Test
  fun resolves_monthly_week_path_from_date() {
      val key = WeekPathResolver.resolve(LocalDate.of(2026, 5, 24))
      assertEquals("src/content/posts/summary/2026year/5month/4week.md", key.repoPath)
  }
  ```

- [ ] **Step 2: 实现最小逻辑**
  - `WeekKey` 保存 `year`、`month`、`weekIndex`
  - `WeekPathResolver` 把日期映射到 `YYYYyear/Mmonth/Nweek.md`
  - `DiaryMarkdownCodec` 解析 frontmatter + 周标题 + 日级小节，并能重新生成 Markdown

- [ ] **Step 3: 跑测试**
  - Run: `./gradlew :app:testDebugUnitTest --tests "*WeekPathResolverTest" --tests "*DiaryMarkdownCodecTest"`
  - Expected: 两个测试类全部通过

- [ ] **Step 4: 提交**
  - `git add app/src/main/java/com/cypress/diary/model app/src/main/java/com/cypress/diary/parser app/src/test/java/com/cypress/diary/parser`
  - `git commit -m "feat: add diary markdown model and codec"`

## Task 3: 接入 GitHub 登录和仓库读写

**Files:**
- Create: `app/src/main/java/com/cypress/diary/auth/GitHubDeviceAuthManager.kt`
- Create: `app/src/main/java/com/cypress/diary/auth/GitHubAuthState.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/TokenStore.kt`
- Create: `app/src/main/java/com/cypress/diary/github/GitHubApi.kt`
- Create: `app/src/main/java/com/cypress/diary/github/GitHubContentsRepository.kt`
- Create: `app/src/test/java/com/cypress/diary/auth/GitHubDeviceAuthManagerTest.kt`
- Create: `app/src/test/java/com/cypress/diary/github/GitHubContentsRepositoryTest.kt`

- [ ] **Step 1: 写登录和读写测试**
  ```kotlin
  @Test
  fun device_flow_exchanges_code_for_token() = runTest {
      val result = authManager.login()
      assertTrue(result is GitHubAuthState.SignedIn)
  }
  ```

  ```kotlin
  @Test
  fun loads_existing_markdown_from_contents_api() = runTest {
      val file = repository.load("src/content/posts/summary/2026year/5month/4week.md")
      assertTrue(file.content.contains("2026-05-24"))
  }
  ```

- [ ] **Step 2: 实现 Device Flow**
  - `GitHubDeviceAuthManager` 请求 device code，轮询 token
  - `TokenStore` 安全保存 access token
  - `GitHubAuthState` 表示未登录、等待授权、已登录、失败

- [ ] **Step 3: 实现 Contents API**
  - 读：根据 repo path 获取 base64 内容并解码
  - 写：提交更新时带上最新 `sha`
  - 统一处理 404、401、429 和普通网络失败

- [ ] **Step 4: 跑测试**
  - Run: `./gradlew :app:testDebugUnitTest --tests "*GitHubDeviceAuthManagerTest" --tests "*GitHubContentsRepositoryTest"`
  - Expected: 全部通过

- [ ] **Step 5: 提交**
  - `git add app/src/main/java/com/cypress/diary/auth app/src/main/java/com/cypress/diary/storage app/src/main/java/com/cypress/diary/github app/src/test/java/com/cypress/diary/auth app/src/test/java/com/cypress/diary/github`
  - `git commit -m "feat: add github device login and contents api"`

## Task 4: 建立离线草稿库和同步协调器

**Files:**
- Create: `app/src/main/java/com/cypress/diary/storage/DraftEntity.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/DraftDao.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/DiaryDatabase.kt`
- Create: `app/src/main/java/com/cypress/diary/storage/DraftStore.kt`
- Create: `app/src/main/java/com/cypress/diary/feature/editor/DiarySyncCoordinator.kt`
- Create: `app/src/test/java/com/cypress/diary/storage/DraftStoreTest.kt`
- Create: `app/src/test/java/com/cypress/diary/feature/editor/DiarySyncCoordinatorTest.kt`

- [ ] **Step 1: 先写草稿和同步测试**
  ```kotlin
  @Test
  fun saves_draft_locally_before_sync() = runTest {
      draftStore.save("path.md", "draft content")
      assertEquals("draft content", draftStore.load("path.md"))
  }
  ```

  ```kotlin
  @Test
  fun coordinator_creates_missing_week_then_uploads() = runTest {
      val result = coordinator.saveDay(date, draft)
      assertTrue(result is SaveResult.Success)
  }
  ```

- [ ] **Step 2: 实现 Room 草稿缓存**
  - 按 `repoPath` 存储草稿正文、更新时间、同步状态
  - 打开编辑页时优先回填草稿
  - 保存时先写本地，再发起远端同步

- [ ] **Step 3: 实现同步协调器**
  - 输入：日期、编辑内容、当前 token、仓库信息
  - 输出：同步成功、等待重试、登录失效等状态
  - 新周时自动走创建文件流程

- [ ] **Step 4: 跑测试**
  - Run: `./gradlew :app:testDebugUnitTest --tests "*DraftStoreTest" --tests "*DiarySyncCoordinatorTest"`
  - Expected: 全部通过

- [ ] **Step 5: 提交**
  - `git add app/src/main/java/com/cypress/diary/storage app/src/main/java/com/cypress/diary/feature/editor app/src/test/java/com/cypress/diary/storage app/src/test/java/com/cypress/diary/feature/editor`
  - `git commit -m "feat: add draft cache and sync coordinator"`

## Task 5: 搭好底部导航、三大页面和编辑页

**Files:**
- Modify: `app/src/main/java/com/cypress/diary/DiaryApp.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/navigation/DiaryRoute.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/navigation/BottomBar.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/screens/DiaryScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/screens/SummaryScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/screens/ProfileScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/screens/EditorScreen.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/DiaryCard.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/components/WeekTree.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/viewmodel/DiaryViewModel.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/viewmodel/SummaryViewModel.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/viewmodel/ProfileViewModel.kt`
- Create: `app/src/main/java/com/cypress/diary/ui/viewmodel/EditorViewModel.kt`
- Create: `app/src/androidTest/java/com/cypress/diary/ui/DiaryScreensTest.kt`

- [ ] **Step 1: 先写 Compose 冒烟测试**
  ```kotlin
  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun bottom_bar_shows_three_tabs() {
      composeRule.setContent { DiaryApp() }
      composeRule.onNodeWithText("diary").assertExists()
      composeRule.onNodeWithText("summary").assertExists()
      composeRule.onNodeWithText("me").assertExists()
  }
  ```

- [ ] **Step 2: 实现底部导航**
  - 三个入口固定为：日记、总结、我的
  - 日记页右下角保留编辑 FAB

- [ ] **Step 3: 实现三页内容**
  - 日记页：按天浏览、上一天/下一天、卡片式正文
  - 总结页：年/月/周树形展开
  - 我的页：GitHub 状态、仓库信息、同步状态、退出登录

- [ ] **Step 4: 实现编辑页**
  - 可编辑当前日内容或整个周记文件
  - 保存时调用同步协调器

- [ ] **Step 5: 跑 UI 测试**
  - Run: `./gradlew :app:connectedDebugAndroidTest`
  - Expected: 三个入口和编辑入口都能渲染出来

- [ ] **Step 6: 提交**
  - `git add app/src/main/java/com/cypress/diary/ui app/src/main/java/com/cypress/diary/ui/viewmodel app/src/androidTest/java/com/cypress/diary/ui`
  - `git commit -m "feat: add diary summary profile and editor screens"`

## Task 6: 端到端验证和收尾

**Files:**
- Modify: 以上所有已创建文件
- Create: `README.md`

- [ ] **Step 1: 跑完整测试**
  - Run: `./gradlew testDebugUnitTest`
  - Run: `./gradlew assembleDebug`
  - Expected: 都成功

- [ ] **Step 2: 手工验证关键流程**
  - 登录 GitHub
  - 打开某一天的日记
  - 编辑并保存
  - 切换到新的一周，确认能自动建文件
  - 打开总结页和我的页

- [ ] **Step 3: 修正最后的布局和文案问题**
  - 保证底部导航文本不溢出
  - 保证日记卡片在手机窄屏下能完整显示
  - 保证编辑页保存按钮和同步状态清晰可见

- [ ] **Step 4: 提交**
  - `git add .`
  - `git commit -m "feat: finish diary app v1"`

## 覆盖检查

- GitHub 登录 -> Task 3
- 日记/总结/我的 三页 -> Task 5
- 编辑已有周记 -> Task 4 + Task 5
- 新的一周自动建文件 -> Task 2 + Task 4
- 直接同步到 GitHub、无后端 -> 全局架构 + Task 3/4
- 离线草稿 -> Task 4
- 测试覆盖 -> Task 2/3/4/5/6
