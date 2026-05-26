# 账单导入导出设计

## 背景

记账模块当前只把账单记录保存在本机 `AccountingRecordStore` 中。用户希望账单也能像日记一样导出和导入，方便备份、换机和迁移。

## 目标

- 在“我的”页提供账单导出和账单导入入口。
- 导出账单为独立文件，不混入日记 `.diary`。
- 导入时让用户选择“替换本地账单”或“合并到账单”。
- 保持账单数据独立于日记 Markdown、GitHub 同步和 `.diary` 导入导出。

## 非目标

- 不把账单导入导出接入 GitHub。
- 不修改日记 `.diary` 格式。
- 不使用 CSV 作为主格式，避免丢失账单 id、创建时间和更新时间。
- 不在导入时做复杂冲突解决界面；同 id 冲突按明确规则处理。

## 文件格式

导出文件扩展名为 `.accounting`，本质是 zip 包。

建议结构：

```text
manifest.json
records.json
```

`manifest.json` 内容：

- `formatVersion`
- `exportedAt`
- `recordCount`

`records.json` 保存账单数组，每条记录包含：

- `id`
- `type`
- `amountCents`
- `category`
- `date`
- `note`
- `createdAt`
- `updatedAt`

格式版本从 `1` 开始。读取时忽略未知字段，遇到损坏记录时跳过该条，不让整个应用崩溃。

## 用户体验

入口放在“我的”页的数据区域，和日记数据按钮分开显示：

- 导出账单数据
- 导入账单数据

导出：

- 通过系统文件创建器生成 `accounting-export-YYYY-MM-DD.accounting`。
- 如果当前没有账单，提示“没有可导出的账单数据”。
- 导出成功后提示“账单数据已导出”。

导入：

- 用户选择 `.accounting` 文件。
- 读取成功后弹窗显示记录数量，并提供三个操作：
  - 替换本地账单
  - 合并到账单
  - 取消
- 替换会用文件里的记录覆盖本地全部账单。
- 合并会保留本地独有记录；同 id 记录以导入文件为准。

## 数据流

新增 `AccountingExportArchive`，职责类似 `DiaryExportArchive`，但只处理 `AccountingRecord`。

导出流程：

1. `DiaryApp` 从当前内存 `accountingRecords` 读取账单。
2. `AccountingExportArchive.write(records, outputStream)` 写入 zip。
3. 成功后不修改本地账单状态。

导入流程：

1. `AccountingExportArchive.read(inputStream)` 返回文件里的记录列表。
2. `DiaryApp` 暂存待导入记录并显示导入模式弹窗。
3. 用户选择替换或合并。
4. 调用 `AccountingRecordStore.saveRecords()` 写入结果。
5. 重新加载 `accountingRecords`，让账本和统计立即更新。

## 合并规则

合并时按 `id` 去重：

- 本地存在、导入不存在：保留本地记录。
- 本地不存在、导入存在：新增导入记录。
- 本地和导入都存在同 id：使用导入记录覆盖本地记录。

写入前统一使用 `sortRecordsForLedger()` 排序。

## 错误处理

- 文件无法打开：提示“无法打开导入文件”。
- 文件格式不正确或没有可读取记录：提示“没有读取到账单数据”。
- 导出或导入失败：提示失败原因，原因为空时显示“未知错误”。
- 损坏单条记录不影响其他记录读取。

## 测试

新增或扩展单元测试：

- `AccountingExportArchiveTest` 写出 manifest 和 records。
- `AccountingExportArchiveTest` 能读回导出的账单。
- 读取损坏记录时跳过坏记录，保留好记录。
- 合并函数按 id 保留本地独有记录，并用导入记录覆盖同 id 本地记录。
- 替换函数结果只包含导入记录。

验证命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## 文档更新

实现完成后更新 `docs/AI_PROJECT_GUIDE.md`：

- 记账模块支持独立 `.accounting` 导入导出。
- `.accounting` 不等于 `.diary`，两者不要混用。
- 导入账单支持替换或合并，合并按 id 去重。
