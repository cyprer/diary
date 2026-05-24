package org.junit

object Assert {
    @JvmStatic
    fun assertEquals(expected: Any?, actual: Any?) {
        if (expected != actual) {
            fail("Expected <$expected> but was <$actual>")
        }
    }

    @JvmStatic
    fun assertEquals(message: String, expected: Any?, actual: Any?) {
        if (expected != actual) {
            fail("$message: expected <$expected> but was <$actual>")
        }
    }

    @JvmStatic
    fun fail(message: String): Nothing {
        throw AssertionError(message)
    }
}
