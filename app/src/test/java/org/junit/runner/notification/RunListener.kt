package org.junit.runner.notification

import org.junit.runner.Description

open class RunListener {
    open fun testStarted(description: Description) = Unit
    open fun testFinished(description: Description) = Unit
    open fun testFailure(failure: Failure) = Unit
    open fun testIgnored(description: Description) = Unit
}
