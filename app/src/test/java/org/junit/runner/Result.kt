package org.junit.runner

import org.junit.runner.notification.Failure

class Result {
    private val failures = mutableListOf<Failure>()
    var runCount: Int = 0
        private set

    fun incrementRunCount() {
        runCount++
    }

    fun addFailure(failure: Failure) {
        failures += failure
    }

    fun getFailures(): List<Failure> = failures.toList()

    fun getFailureCount(): Int = failures.size

    fun wasSuccessful(): Boolean = failures.isEmpty()
}
