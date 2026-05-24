package com.cypress.diary.parser

import com.cypress.diary.model.WeekKey
import java.time.LocalDate

class WeekPathResolver {
    fun resolve(date: LocalDate): String = resolve(WeekKey.from(date))

    fun resolve(key: WeekKey): String {
        val yearFolder = key.year % 100
        return "src/content/posts/summary/${yearFolder}year/${key.month}month/${key.weekIndex}week.md"
    }
}
