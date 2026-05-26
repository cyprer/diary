# 总结文档与背景图修复设计

## 目标

调整安卓日记 App 的总结页，使它匹配桌面 `astroblog` 项目的真实
`summary` 目录结构：

- 年度总结位于 `src/content/posts/summary/YYyear/index.md`。
- 月度总结位于 `src/content/posts/summary/YYyear/Mmonth/index.md`。
- 周记位于 `src/content/posts/summary/YYyear/Mmonth/Nweek.md`。

总结页需要读取这些 Markdown 文件。用户点击年份、月份或周时，默认看到
渲染后的阅读内容；只有点击编辑按钮后，才进入完整 Markdown 原文编辑和推送。

同时修复背景图功能，使用户选择的背景图片能正常显示，并在图片读取失败时
优雅降级为普通主题背景。

## 总结页交互

总结页有两类点击行为：

- 展开控制：点击年份或月份旁边的箭头/两侧区域，展开或收起子节点。
- 内容选择：点击年份、月份或周的文字区域，选中对应文档并显示其渲染内容。

节点行为如下：

- 点击年份文字：显示该年的 `index.md` 渲染内容。
- 点击年份展开控制：展开月份。
- 点击月份文字：显示该月的 `index.md` 渲染内容。
- 点击月份展开控制：展开四个周文件。
- 点击第几周：显示对应 `Nweek.md` 的渲染内容。
- 周节点不再继续展开到每天。

选中文档后，页面展示的是阅读模式，不直接展示 Markdown 原文。右下角提供
编辑按钮，点击后打开当前选中文档的完整 Markdown 原文。

## 编辑与推送

从总结页进入编辑时，沿用现有 GitHub 推送链路：

- 编辑页接收当前选中文档的仓库路径和完整 Markdown 原文。
- 草稿按文档路径保存。
- 推送时把编辑后的 Markdown 写回同一个 GitHub 路径。
- 推送成功后刷新远端文档，并更新本地缓存。

日记页按天编辑的现有能力保留。总结页的编辑是按文件路径编辑，不尝试只合并
某一天的小节内容。

## 数据模型

新增通用总结文档模型：

- `DiaryDocument`
  - `path`
  - `type`：年度总结、月度总结或周记
  - `year`
  - 可选 `month`
  - 可选 `weekIndex`
  - `title`
  - `published`
  - `markdown`
  - `body`

现有 `DiaryWeek` 仍然用于日记页和周记解析。总结页改为基于 `DiaryDocument`
构建树结构，这样年/月 `index.md` 和周记文件可以统一处理。

## GitHub 读取

对于带 Token 的仓库，递归读取 Git tree，并纳入以下文件：

- `src/content/posts/summary/YYyear/index.md`
- `src/content/posts/summary/YYyear/Mmonth/index.md`
- `src/content/posts/summary/YYyear/Mmonth/Nweek.md`

对于不带 Token 的公开仓库，按 `astroblog` 的固定结构推导候选路径：

- 年份从 2025 到当前年份。
- 当前年份只推导到当前月份。
- 每个年份包含一个 `index.md`。
- 每个月份包含一个 `index.md`。
- 每个月份包含 `1week.md` 到 `4week.md`。

公开仓库中不存在的候选文件直接忽略。

## Markdown 渲染

本版本使用轻量渲染，不额外引入完整 Markdown 渲染依赖：

- 去掉 frontmatter。
- 如果正文第一个 H1 与文档标题重复，则阅读模式中只保留一个标题。
- 用 Compose 文本样式渲染标题和段落。
- 保留可读的换行。

这样可以先得到干净的阅读模式，同时避免为了这个版本引入额外复杂度。

## 背景图修复

当前背景图几乎不可见，原因是图片先以很低透明度绘制，然后又被一层接近不透明
的主题背景盖住。

调整后的行为：

- 选中背景图时，图片以原始不透明度全屏铺底，并使用 `ContentScale.Crop`。
- 在图片上方覆盖一层半透明主题色遮罩，保证文字可读。
- 没有选择背景图时，使用普通纯色主题背景。
- 图片读取放到后台线程。
- URI 读取失败时不崩溃，直接回退到普通主题背景。

## 测试

新增聚焦单元测试：

- 年/月/周 summary 路径匹配与信息提取。
- 总结树构建包含年总结、月总结和周节点，周节点不再展开到天。
- Markdown 文档从 frontmatter 中提取阅读正文。
- 文档缓存按路径保存和读取。

现有周记解析、路径计算、草稿、主题相关测试都需要继续通过。
