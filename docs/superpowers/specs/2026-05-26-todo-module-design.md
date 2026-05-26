# 待办清单模块设计

## 背景

当前应用已有日记和记账两个主模块，分别覆盖记录生活内容和收支数据。待办清单属于独立的生活管理场景，需要独立列表、编辑页、筛选逻辑和本地持久化。将待办做成第三个主模块，可以沿用现有 `AppModule`、`DiaryRoute`、底部导航和 `SharedPreferences` 存储模式，避免把任务管理能力塞进“我的”或日记页面导致职责混杂。

## 目标

- 新增“待办”主模块，与“日记”“记账”同级。
- 支持新增、编辑、删除待办。
- 支持完成和取消完成。
- 支持任务标题、备注、截止日期、优先级和完成状态。
- 支持按“全部 / 今天 / 未来 / 已完成”筛选。
- 使用本地持久化，应用重启后待办仍可恢复。
- 补充 focused 单元测试覆盖存储、排序、筛选和路由集成。

## 非目标

- 不做系统提醒、通知权限或闹钟。
- 不做重复任务。
- 不做多项目/多清单分组。
- 不做云同步、GitHub 同步、导入导出。
- 不把待办混入日记 `.diary` 或记账 `.accounting` 文件。

## 用户体验

待办模块的底部导航：

- `待办`：待办列表首页。
- `我的`：复用现有个人设置页。

待办列表页：

- 顶部显示筛选项：全部、今天、未来、已完成。
- 列表中每条待办显示标题、备注摘要、截止日期、优先级和完成状态。
- 未完成任务排在已完成任务前。
- 点击任务进入编辑页。
- 点击完成控件可快速切换完成状态。
- 浮动新增按钮进入新建待办页。
- 空列表显示轻量空状态文案。

待办编辑页：

- 标题必填。
- 备注可选。
- 截止日期可选，支持清除。
- 优先级使用低 / 中 / 高。
- 编辑已有待办时显示删除操作。
- 保存后回到待办列表。

## 数据模型

新增 `TodoItem`：

- `id: String`
- `title: String`
- `note: String`
- `dueDate: LocalDate?`
- `priority: TodoPriority`
- `completed: Boolean`
- `createdAt: Long`
- `updatedAt: Long`
- `completedAt: Long?`

新增 `TodoPriority`：

- `Low`
- `Medium`
- `High`

优先级只影响列表排序和视觉标记，不影响提醒或统计。

## 排序和筛选

排序规则：

1. 未完成任务优先。
2. 未完成任务中，有截止日期的排在无截止日期前。
3. 截止日期越早越靠前。
4. 同一截止日期内，高优先级排在中、低优先级前。
5. 已完成任务排在后面，按 `updatedAt` 倒序。

筛选规则：

- 全部：所有待办。
- 今天：未完成且 `dueDate == selectedDate`。
- 未来：未完成且 `dueDate > selectedDate`，无截止日期也显示在未来列表后段。
- 已完成：所有已完成待办。

`selectedDate` 使用应用当前的 `selectedDate` 状态，和日记/记账的日期语义保持一致。

## 存储设计

新增 `TodoItemStore`：

- 使用独立 SharedPreferences，例如 `todo_items`。
- 存储格式沿用记账 store 的一行一条记录模式。
- 字段使用 Base64 包装字符串，避免标题和备注中的分隔符破坏解析。
- 读取时跳过损坏行，不让单条坏数据导致整个列表不可用。
- 提供 `loadItems()`、`saveItems()`、`upsert()`、`delete()`。

## 集成设计

导航：

- `AppModule` 新增 `Todo("待办")`。
- `DiaryRoute` 新增 `TodoList("todo", "待办", Icons.Filled.CheckCircle)`。
- `DiaryRoute` 新增 `TodoEditor("todo_editor", "编辑待办", Icons.Filled.Edit)`。
- `rootRoutesFor(AppModule.Todo)` 返回 `TodoList` 和 `Profile`。

应用状态：

- `DiaryApp` 初始化 `TodoItemStore`。
- 维护 `todoItems`、`selectedTodoItemId`、`todoFilterName`。
- 新增保存、删除、切换完成状态的回调。

UI：

- 新增 `TodoListScreen`。
- 新增 `TodoEditorScreen`。
- 优先复用现有 Material 3、Card、chip、FAB、RefreshableScreen 风格。

## 错误处理

- 标题为空时禁用保存或显示输入错误。
- 存储读取遇到坏行时跳过。
- 编辑页找不到目标待办时按新建页处理，避免崩溃。
- 删除待办后清空选中 id 并返回列表。

## 测试

新增或扩展单元测试：

- `TodoItemStoreTest`：保存、读取、更新、删除。
- `TodoItemStoreTest`：跳过损坏行。
- `TodoItemSortTest`：未完成优先、日期排序、优先级排序、已完成倒序。
- `TodoFilterTest`：全部、今天、未来、已完成。
- `DiaryRouteTest`：待办根路由和编辑路由。
- `AppModuleStoreTest`：保存和恢复 `Todo` 模块。

验证命令：

```powershell
.\gradlew.bat testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

## 文档和发布

实现完成后更新 `README.md` 的功能说明，提到待办模块。若生成新的 APK，应重新构建并覆盖 `release/dailylife.apk`，必要时更新 GitHub Release 资产。
