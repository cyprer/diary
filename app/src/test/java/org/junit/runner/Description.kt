package org.junit.runner

data class Description(
    val displayName: String,
) {
    companion object {
        @JvmStatic
        fun createTestDescription(testClass: Class<*>, methodName: String): Description {
            return Description("${testClass.name}#$methodName")
        }
    }
}
