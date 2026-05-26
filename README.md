<div align="right">
  <a href="#readme-en">English</a> | <a href="#readme-zh">中文</a>
</div>

---

<a id="readme-en"></a>

# Diary

A personal diary Android app with **Markdown journaling** and **accounting (expense/income tracker)** modules, built with Jetpack Compose.

## Features

### Diary Module
- **Daily journal** — write Markdown-formatted diary entries for each day
- **Calendar navigation** — year/month/week pickers with filter chips
- **Editor** — day and week editing modes with auto-saved drafts
- **Full-text search** — search across diary entries
- **Summary documents** — yearly, monthly, weekly summaries with word-count charts
- **Daily quote** — fetches from Hitokoto API
- **GitHub sync** (hidden) — optional sync with a GitHub repo, unlocked via easter egg
- **Export/Import** — `.diary` ZIP archives for manual migration

### Accounting Module
- **Ledger** — view income/expense records with calendar picker
- **Record editor** — add/edit/delete records with categories
- **Stats** — weekly/monthly/yearly charts with category breakdowns
- **Export/Import** — `.accounting` ZIP archives with merge-or-replace on import

### Appearance
- 4 theme palettes (BlueGray, Mint, Lavender, Graphite)
- Custom background image
- Layout opacity adjustment

## Tech Stack

| Layer | Technology |
|---|---|
| Language | **Kotlin** 1.9.24 |
| UI | **Jetpack Compose** + Material 3 |
| Navigation | navigation-compose 2.7.7 |
| Async | Kotlin Coroutines 1.9.0 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 |
| Build | Gradle (Kotlin DSL), AGP 8.4.2 |

## Build & Run

```powershell
# Run unit tests
.\gradlew.bat testDebugUnitTest --console=plain

# Build debug APK
.\gradlew.bat :app:assembleDebug --console=plain

# Install on device
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## Project Structure

```
app/src/main/java/com/cypress/diary/
├── MainActivity.kt            — Android entry point
├── DiaryApp.kt                — Compose root app
├── model/                     — DiaryWeek, DiaryDay, WeekKey, accounting models
├── parser/                    — Markdown codec, document codec, path resolver
├── github/                    — GitHub API client & config (hidden feature)
├── storage/                   — SharedPreferences caches, drafts, settings
├── accounting/                — Amount parsing, stats logic
├── export/                    — .diary / .accounting ZIP export/import
├── quote/                     — Hitokoto daily quote API
├── ui/
│   ├── screens/               — Diary, Summary, Editor, Profile, Accounting screens
│   ├── calendar/              — Year/Month/Week pickers
│   ├── editor/                — Editor & draft merge logic
│   ├── summary/               — Summary tree & week day filtering
│   ├── components/            — Shared UI components
│   └── navigation/            — Routes & module definitions
```

## License

MIT

---

<a id="readme-zh"></a>

<div align="right">
  <a href="#readme-en">English</a> | <a href="#readme-zh">中文</a>
</div>

# Diary

一款基于 Jetpack Compose 的 Android 个人日记应用，包含**日记**和**记账**两个模块。

## 功能

### 日记模块
- **每日日记** — 使用 Markdown 格式撰写每日日记
- **日历导航** — 年/月/周选择器，支持快速切换
- **编辑器** — 支持日编辑和周编辑模式，自动保存草稿
- **全文搜索** — 搜索日记内容
- **总结文档** — 年结/月结/周结，配字数统计折线图
- **每日一言** — 从 Hitokoto API 获取
- **GitHub 同步**（隐藏功能）— 可选 GitHub 仓库同步，通过彩蛋解锁
- **导入/导出** — `.diary` ZIP 文件，用于手动迁移

### 记账模块
- **账本** — 按日期查看收支记录
- **记录编辑** — 新增/编辑/删除账目，支持自定义分类
- **统计** — 周/月/年统计图表，分类排行
- **导入/导出** — `.accounting` ZIP 文件，支持合并或替换导入

### 外观
- 4 种主题色（蓝灰、薄荷、薰衣草、石墨）
- 自定义背景图片
- 布局透明度调节

## 技术栈

| 层 | 技术 |
|---|---|
| 语言 | **Kotlin** 1.9.24 |
| UI | **Jetpack Compose** + Material 3 |
| 导航 | navigation-compose 2.7.7 |
| 异步 | Kotlin Coroutines 1.9.0 |
| 最低 SDK | 24 (Android 7.0) |
| 目标 SDK | 34 |
| 构建 | Gradle (Kotlin DSL), AGP 8.4.2 |

## 构建与运行

```powershell
# 运行单元测试
.\gradlew.bat testDebugUnitTest --console=plain

# 构建 debug APK
.\gradlew.bat :app:assembleDebug --console=plain

# 安装到设备
adb install -r app\build\outputs\apk\debug\app-debug.apk
```

## 项目结构

```
app/src/main/java/com/cypress/diary/
├── MainActivity.kt            — Android 入口
├── DiaryApp.kt                — Compose 应用根组件
├── model/                     — DiaryWeek, DiaryDay, WeekKey, 记账模型
├── parser/                    — Markdown 编解码、文档编解码、路径解析
├── github/                    — GitHub API 客户端与配置（隐藏功能）
├── storage/                   — SharedPreferences 缓存、草稿、设置
├── accounting/                — 金额解析、统计逻辑
├── export/                    — .diary / .accounting ZIP 导入导出
├── quote/                     — Hitokoto 每日一言 API
├── ui/
│   ├── screens/               — 日记、总结、编辑、我的、记账等页面
│   ├── calendar/              — 年/月/周选择器
│   ├── editor/                — 编辑器与草稿合并逻辑
│   ├── summary/               — 总结树与周日记筛选
│   ├── components/            — 共享 UI 组件
│   └── navigation/            — 路由与模块定义
```

## 许可证

MIT
