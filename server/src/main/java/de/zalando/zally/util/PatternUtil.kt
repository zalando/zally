package de.zalando.zally.util

/**
 * Utility library for matching common patterns
 */
object PatternUtil {

    private val PATH_VARIABLE_PATTERN = "\\{.+}$".toRegex()

    fun isPathVariable(input: String): Boolean = input.matches(PATH_VARIABLE_PATTERN)
}
