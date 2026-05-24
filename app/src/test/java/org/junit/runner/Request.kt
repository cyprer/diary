package org.junit.runner

class Request private constructor(
    val testClasses: Array<out Class<*>>,
) {
    companion object {
        @JvmStatic
        fun classes(vararg testClasses: Class<*>): Request = Request(testClasses.copyOf())
    }
}
