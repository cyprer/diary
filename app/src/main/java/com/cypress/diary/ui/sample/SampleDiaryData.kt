package com.cypress.diary.ui.sample

import com.cypress.diary.model.DiaryDay
import com.cypress.diary.model.DiaryWeek
import com.cypress.diary.model.WeekKey
import java.time.LocalDate

fun sampleDiaryWeeks(): List<DiaryWeek> {
    return listOf(
        week(
            key = WeekKey(2026, 5, 4),
            title = "第四周周记",
            published = LocalDate.of(2026, 5, 22),
            intro = "把零散的日常先写下来，再慢慢整理成一周的总结。",
            days = listOf(
                day(
                    date = LocalDate.of(2026, 5, 22),
                    content = """
                        今天把五月最后一周的计划重新整理了一遍，先把要做的事收束成几个明确的小任务。

                        晚上散步的时候状态不错，路上想清楚了明天要先处理的两件事。
                    """.trimIndent(),
                ),
                day(
                    date = LocalDate.of(2026, 5, 23),
                    content = """
                        早上复盘了博客项目里的周记文件，确认每个月只保留四周的写法更适合现在的习惯。

                        下午把一些零碎记录合并进周记，内容看起来清爽了很多。
                    """.trimIndent(),
                ),
                day(
                    date = LocalDate.of(2026, 5, 24),
                    content = """
                        今天先把日记 App 的主要页面跑起来，让手机上打开时能直接看到当天内容。

                        总结页先按年份、月份、周次展开，后面接 GitHub 同步时会直接复用这套结构。

                        晚上准备继续补编辑和同步的细节，先保证写日记这件事足够顺手。
                    """.trimIndent(),
                ),
            ),
        ),
        week(
            key = WeekKey(2026, 5, 3),
            title = "第三周周记",
            published = LocalDate.of(2026, 5, 15),
            intro = "这一周主要是把学习和项目节奏重新排了一下。",
            days = listOf(
                day(LocalDate.of(2026, 5, 15), "完成了课程设计报告的整理，顺手把代码说明也补全了。"),
                day(LocalDate.of(2026, 5, 18), "把博客里几个旧页面重新检查了一遍，发现周记归档规则还需要统一。"),
                day(LocalDate.of(2026, 5, 21), "晚上复盘近期任务，决定把移动端写作工具先做出来。"),
            ),
        ),
        week(
            key = WeekKey(2025, 1, 1),
            title = "第一周周记",
            published = LocalDate.of(2025, 1, 1),
            intro = "新的一年从一个小计划开始。",
            days = listOf(
                day(LocalDate.of(2025, 1, 1), "元旦，整理房间，写下今年想完成的几件事。"),
                day(LocalDate.of(2025, 1, 2), "制定了新年计划，把年度目标拆成几个阶段。"),
                day(LocalDate.of(2025, 1, 3), "完成了年度目标拆解，感觉方向比昨天更清楚。"),
                day(LocalDate.of(2025, 1, 4), "阅读了一会儿书，记下几段有用的想法。"),
                day(LocalDate.of(2025, 1, 5), "晨跑 5 公里，状态不错。"),
                day(LocalDate.of(2025, 1, 6), "和朋友聚会，聊了很多收获。"),
                day(LocalDate.of(2025, 1, 7), "整理房间，准备迎接新的一周。"),
            ),
        ),
        week(
            key = WeekKey(2025, 1, 4),
            title = "第四周周记",
            published = LocalDate.of(2025, 1, 22),
            intro = "一月收尾，把月底的事情集中处理掉。",
            days = listOf(
                day(LocalDate.of(2025, 1, 22), "整理月底计划，先处理最容易拖延的部分。"),
                day(LocalDate.of(2025, 1, 31), "给一月做了总结，准备把二月计划写得更具体。"),
            ),
        ),
        week(
            key = WeekKey(2025, 2, 1),
            title = "第一周周记",
            published = LocalDate.of(2025, 2, 1),
            intro = "二月第一周，从 2 月 1 日开始记录。",
            days = listOf(
                day(LocalDate.of(2025, 2, 1), "重新确认周记文件规则，第一周从本月第一天开始。"),
                day(LocalDate.of(2025, 2, 7), "把这周的零碎记录归档完成。"),
            ),
        ),
    )
}

private fun week(
    key: WeekKey,
    title: String,
    published: LocalDate,
    intro: String,
    days: List<DiaryDay>,
): DiaryWeek {
    return DiaryWeek(
        key = key,
        title = title,
        intro = intro,
        published = published,
        description = title,
        tags = listOf("周报", "总结"),
        category = "周报",
        draft = false,
        days = days,
    )
}

private fun day(date: LocalDate, content: String): DiaryDay {
    return DiaryDay(date = date, content = content)
}
