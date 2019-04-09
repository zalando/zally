package de.zalando.zally.util

/**
 * Utility library for matching common patterns
 */
object PatternUtil {

    private val PATH_VARIABLE_PATTERN = "\\{.+}$".toRegex()
    private val APPLICATION_PROBLEM_JSON_PATTERN = "^application/(problem\\+)?json$".toRegex()
    private val CUSTOM_WITH_VERSIONING_PATTERN = "^\\w+/[-+.\\w]+;v(ersion)?=\\d+$".toRegex()

    fun isPathVariable(input: String): Boolean = input.matches(PATH_VARIABLE_PATTERN)

    fun isApplicationJsonOrProblemJson(mediaType: String): Boolean =
        mediaType.matches(APPLICATION_PROBLEM_JSON_PATTERN)

    fun isCustomMediaTypeWithVersioning(mediaType: String): Boolean =
        mediaType.matches(CUSTOM_WITH_VERSIONING_PATTERN)
}
