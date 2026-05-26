# 待办提醒设计

## 目标

- 待办可设置提醒时间。
- 关闭 app 后仍能在提醒时间触发。
- 使用闹钟式系统能力，尽量在锁屏或后台场景中弹出高优先级提醒。
- 兼容 Android 13+ 通知权限。
- 删除待办、完成待办或移除提醒时间时取消已安排提醒。
- 设备重启后重新安排未完成待办的未来提醒。

## 技术方案

采用 `AlarmManager.setAlarmClock` 安排提醒。它适合用户可见的闹钟/提醒事件，比普通后台任务更符合“关掉 app 后仍提醒”的需求。提醒触发后由 `TodoReminderReceiver` 接收广播并通过 `NotificationManager` 发出高优先级通知。通知使用 `CATEGORY_REMINDER`、高优先级 channel 和 full-screen intent；Android 是否以全屏弹窗呈现由系统策略决定，但锁屏/闹钟类场景会比普通通知更接近弹窗体验。

## 权限

- `POST_NOTIFICATIONS`：Android 13+ 需要运行时申请，否则通知不会显示。
- `USE_FULL_SCREEN_INTENT`：允许 full-screen intent 通知。
- `RECEIVE_BOOT_COMPLETED`：重启后恢复未来提醒。

不申请 `SCHEDULE_EXACT_ALARM`。本实现使用 `setAlarmClock`，它是用户可见闹钟式提醒，避免把普通待办强行依赖特殊精确闹钟权限。

## 数据模型

`TodoItem` 新增：

- `reminderAtMillis: Long?`

存储编码向后兼容：旧的 9 字段待办记录按 `reminderAtMillis = null` 读取；新的 10 字段记录写入提醒时间。

## 用户体验

待办编辑页新增“提醒时间”：

- 可选无提醒。
- 可一键设置为今天 20:00。
- 可一键设置为明天 09:00。
- 可手动输入 `YYYY-MM-DD HH:mm`。
- 输入过去时间或格式错误时不允许保存。

待办列表显示提醒时间摘要。完成待办后自动取消提醒；取消完成且提醒时间仍在未来时重新安排。

## 测试

- `TodoItemStoreTest` 覆盖提醒时间保存/读取和旧数据兼容。
- `TodoReminderTimeTest` 覆盖提醒时间解析、格式化、过去时间判断。
- 现有完整单元测试和 debug APK 构建作为最终验证。
