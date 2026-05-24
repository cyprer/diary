package org.junit.runner.notification

import org.junit.runner.Description

data class Failure(
    val description: Description,
    val exception: Throwable,
)
