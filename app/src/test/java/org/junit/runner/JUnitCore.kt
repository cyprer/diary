package org.junit.runner

import org.junit.Test
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunListener
import java.lang.reflect.InvocationTargetException

class JUnitCore {
    private val listeners = mutableListOf<RunListener>()

    fun addListener(listener: RunListener) {
        listeners += listener
    }

    fun removeListener(listener: RunListener) {
        listeners.remove(listener)
    }

    fun run(vararg testClasses: Class<*>): Result = run(Request.classes(*testClasses))

    fun run(request: Request): Result {
        val result = Result()

        for (testClass in request.testClasses) {
            val instance = testClass.getDeclaredConstructor().apply { isAccessible = true }.newInstance()
            val methods = testClass.declaredMethods
                .filter { it.isAnnotationPresent(Test::class.java) }
                .sortedBy { it.name }

            for (method in methods) {
                val description = Description.createTestDescription(testClass, method.name)
                listeners.forEach { it.testStarted(description) }
                try {
                    method.isAccessible = true
                    method.invoke(instance)
                    result.incrementRunCount()
                } catch (error: InvocationTargetException) {
                    val failure = Failure(description, error.targetException ?: error)
                    result.addFailure(failure)
                    listeners.forEach { it.testFailure(failure) }
                } catch (error: Throwable) {
                    val failure = Failure(description, error)
                    result.addFailure(failure)
                    listeners.forEach { it.testFailure(failure) }
                } finally {
                    listeners.forEach { it.testFinished(description) }
                }
            }
        }

        return result
    }
}
