# dailylife

一个 Android 日记和记账应用，已提供可直接安装的 APK。

## 直接安装

仓库内已放置安装包：

```text
release/dailylife.apk
```

安装方式：

1. 在手机上下载 `release/dailylife.apk`。
2. 打开 APK 文件并允许“安装未知来源应用”。
3. 安装完成后打开 `dailylife`。

也可以通过 ADB 安装：

```powershell
adb install -r release\dailylife.apk
```

## 功能

- 日记：按年、月、周、日查看和编辑日记，支持 Markdown 内容。
- 搜索：按关键词搜索已有日记。
- 总结：支持年总结、月总结、周总结和字数统计。
- 记账：记录收入和支出，支持分类、账本列表和统计图表。
- 数据迁移：日记支持 `.diary` 导入/导出，记账支持 `.accounting` 导入/导出。
- 个性化：支持主题色、背景图片和界面透明度设置。

## 开发者说明

源码保留在仓库中，主要技术栈如下：

- Kotlin
- Jetpack Compose
- Material 3
- Gradle Kotlin DSL
- Android minSdk 24

如需自行构建：

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

构建产物位于：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 仓库内容

- `release/dailylife.apk`：可直接安装的 APK。
- `app/`：Android 应用源码。
- `gradle/`、`build.gradle.kts`、`settings.gradle.kts`：构建配置。

仓库不再保留 AI 生成过程文档、设计草稿和临时说明文件。
