# 记账年度总结设计

## 背景

记账模块目前在底部导航中提供 `账本 / 统计 / 我的`。`统计` 页只支持按月查看收入、支出、结余和分类排行。用户希望统计页既能选择看月度总结，也能选择看年度总结。

## 目标

- 在现有 `统计` 页内提供 `月度 / 年度` 切换。
- 保留现有月度统计行为。
- 新增年度总结视图，支持切换上一年和下一年。
- 年度总结继续使用本地记账记录，不接入 GitHub、Markdown、`.diary` 导入导出。

## 非目标

- 不新增底部导航 tab。
- 不在年度总结中展开全年每一笔账目。
- 不把年度记账数据写入日记总结文档。
- 不做图表库引入；趋势展示优先使用 Compose 内置布局和进度条。

## 用户体验

进入 `记账 -> 统计` 后，页面顶部显示 `月度 / 年度` 切换控件。

月度模式：

- 继续展示当前选择月份。
- 支持上一月、下一月切换。
- 展示月度收入、支出、结余。
- 展示当月支出分类排行和收入分类排行。

年度模式：

- 展示当前选择年份。
- 支持上一年、下一年切换。
- 展示全年收入、支出、结余。
- 展示 12 个月收支趋势，用月份行或条形进度表达每月收入、支出、结余。
- 展示全年支出分类排行和收入分类排行。
- 没有账目时显示空状态，不崩溃、不显示误导性的排行。

## 数据和状态

新增年度统计逻辑放在 `accounting/AccountingSummary.kt`：

- `recordsForYear(records, year)`：筛选某年的记录。
- `yearlySummary(records, year)`：汇总全年收入、支出、结余。
- `monthlyTotalsForYear(records, year)`：生成 1 到 12 月的月度汇总列表，月份无记录时金额为 0。

`AccountingStatsScreen` 增加统计范围状态：

- 月度和年度模式由 UI 内部或 `DiaryApp` 持有的 saveable 状态控制。
- 月度继续使用现有 `accountingMonthValue`。
- 年度可从当前月度年份初始化，并单独保存为 `accountingYearValue`，避免切换月份时意外改变年度视图。

## UI 结构

`AccountingStatsScreen` 继续作为唯一统计页入口。

建议拆分为小组件：

- `AccountingStatsModeSelector`
- `AccountingMonthlyStatsContent`
- `AccountingYearlyStatsContent`
- `CategoryTotalSection`
- `YearMonthTrendSection`

这样月度和年度统计共享分类排行组件，但避免一个 composable 过大。

## 边界和错误处理

- 金额仍使用 cents 作为 Long 计算，显示时继续使用 `formatAmountCents()`。
- 年份范围不做硬限制，用户可以切换到没有数据的年份。
- 年度趋势的最大值为 0 时，进度条显示 0，避免除以 0。
- 年度统计只读本地 `AccountingRecordStore` 已加载到内存的 records，不新增持久化格式。

## 测试

新增或扩展单元测试：

- `recordsForYear()` 只返回目标年份记录。
- `yearlySummary()` 正确汇总全年收入、支出、结余。
- `monthlyTotalsForYear()` 固定返回 12 项，并对无记录月份填 0。
- `categoryTotals()` 可复用年度筛选后的记录，排行顺序保持金额降序、分类名升序。

验证命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## 文档更新

实现完成后更新 `docs/AI_PROJECT_GUIDE.md` 的记账模块说明，明确统计页支持月度和年度两种模式。
