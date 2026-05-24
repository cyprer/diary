package com.cypress.diary

import com.cypress.diary.parser.DiaryMarkdownCodecTest
import com.cypress.diary.parser.WeekPathResolverTest
import org.junit.Test
import java.lang.reflect.InvocationTargetException
import kotlin.system.exitProcess

fun main() {
    val testClasses = listOf(
        WeekPathResolverTest::class.java,
        DiaryMarkdownCodecTest::class.java,
    )

    val failures = mutableListOf<String>()
    var executed = 0

    for (testClass in testClasses) {
        val constructor = testClass.getDeclaredConstructor()
        constructor.isAccessible = true
        val instance = constructor.newInstance()

        for (method in testClass.declaredMethods) {
            if (!method.isAnnotationPresent(Test::class.java)) {
                continue
            }
            method.isAccessible = true
            executed++
            try {
                method.invoke(instance)
                println("PASS ${testClass.simpleName}.${method.name}")
            } catch (error: InvocationTargetException) {
                val cause = error.targetException ?: error
                failures += "FAIL ${testClass.simpleName}.${method.name}: ${cause.message}"
            } catch (error: Throwable) {
                failures += "FAIL ${testClass.simpleName}.${method.name}: ${error.message}"
            }
        }
    }

    println("Executed $executed tests")
    if (failures.isNotEmpty()) {
        failures.forEach(::println)
        exitProcess(1)
    }
}
