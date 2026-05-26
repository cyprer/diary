# 记账自定义分类设计

## 背景

记账模块当前分类来自 `defaultAccountingCategories`，用户录入账单时只能选择固定支出/收入分类。用户希望可以自己新增分类标签，并且这些标签在账单导出后再导入时也能恢复。

## 目标

- 在记账编辑页支持新增自定义分类。
- 自定义分类按账目类型区分：支出分类和收入分类互不混用。
- 新增分类保存到本地，后续录入账单时继续展示。
- 账单导出 `.accounting` 文件时同时导出自定义分类。
- 导入 `.accounting` 文件时恢复自定义分类，并按导入模式合并或替换账单记录。

## 非目标

- 不做分类删除。
- 不做分类重命名。
- 不迁移历史账单分类名；已有记录仍按当时保存的分类文本显示和统计。
- 不把自定义分类接入日记 `.diary` 或 GitHub。

## 用户体验

记账编辑页的“分类”区域：

- 固定分类和用户自定义分类一起显示为 chips。
- chips 最后显示 `+ 新分类`。
- 点击 `+ 新分类` 弹出输入框。
- 当前账目类型是支出时，新增支出分类；当前是收入时，新增收入分类。
- 分类名不能为空，前后空格会 trim。
- 同类型下已有同名分类时不重复新增，直接选中已有分类。
- 新增成功后立刻选中该分类。

切换收入/支出类型时：

- 分类列表切换到对应类型。
- 默认选中该类型第一个可用分类。

## 数据模型和存储

继续使用现有 `AccountingCategory`：

- `key`
- `label`
- `type`

新增 `AccountingCategoryStore`：

- 使用独立 SharedPreferences，例如 `accounting_categories`。
- 只保存用户自定义分类，不重复保存内置默认分类。
- 读取时损坏行跳过。
- 保存时按类型和创建顺序稳定输出。

分类 key：

- 内置分类继续使用当前固定 key。
- 自定义分类 key 使用 `custom_` 加时间戳或 UUID。
- 账单记录仍只保存 `category` 文本，不保存 category key，确保历史记录兼容。

## 导出导入

`.accounting` zip 继续包含：

```text
manifest.json
records.json
categories.json
```

`categories.json` 只保存用户自定义分类。导入旧文件时如果没有 `categories.json`，仍能正常导入账单记录。

导入分类规则：

- 选择“替换本地账单”时，账单记录替换；自定义分类也替换为文件中的自定义分类。
- 选择“合并到账单”时，账单记录按 id 合并；自定义分类按 `type + label` 合并，避免重复。
- 内置默认分类永远不从导入文件删除。

## 测试

新增或扩展单元测试：

- `AccountingCategoryStoreTest` 保存和读取自定义分类。
- `AccountingCategoryStoreTest` 跳过损坏分类行。
- `AccountingExportArchiveTest` 导出并读回自定义分类。
- `AccountingExportArchiveTest` 读取没有 `categories.json` 的旧 `.accounting` 文件仍返回账单。
- 自定义分类合并按 `type + label` 去重。

验证命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## 文档更新

实现完成后更新 `docs/AI_PROJECT_GUIDE.md`，说明自定义分类只存在记账模块，导出导入 `.accounting` 时会一起迁移。
