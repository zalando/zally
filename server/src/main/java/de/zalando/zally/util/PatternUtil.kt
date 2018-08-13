package de.zalando.zally.util

/**
 * Utility library for matching common patterns
 */
object PatternUtil {

    private const val LOWER_CASE_HYPHENS_PATTERN = "^[a-z-]*$"
    private const val CAMEL_CASE_PATTERN = "^[a-z]+(?:[A-Z][a-z]+)*$"
    private const val PASCAL_CASE_PATTERN = "^[A-Z][a-z]+(?:[A-Z][a-z]+)*$"
    private const val HYPHENATED_CAMEL_CASE_PATTERN = "^[a-z]+(?:-[A-Z][a-z]+)*$"
    private const val HYPHENATED_PASCAL_CASE_PATTERN = "^[A-Z][a-z0-9]*(?:-[A-Z][a-z0-9]+)*$"
    private const val SNAKE_CASE_PATTERN = "^[a-z0-9]+(?:_[a-z0-9]+)*$"
    private const val KEBAB_CASE_PATTERN = "^[a-z]+(?:-[a-z]+)*$"
    private const val VERSION_IN_URL_PATTERN = "(.*)/v[0-9]+(.*)"
    private const val PATH_VARIABLE_PATTERN = "\\{.+\\}$"
    private const val GENERIC_VERSION_PATTERN = "^\\d+\\.\\d+\\.\\d+$"
    private const val PATTERN_APPLICATION_PROBLEM_JSON = "^application/(problem\\+)?json$"
    private const val PATTERN_CUSTOM_WITH_VERSIONING = "^\\w+/[-+.\\w]+;v(ersion)?=\\d+$"

    fun hasTrailingSlash(input: String): Boolean {
        return input.trim { it <= ' ' }.endsWith("/")
    }

    fun isLowerCaseAndHyphens(input: String): Boolean {
        return input.matches(LOWER_CASE_HYPHENS_PATTERN.toRegex())
    }

    fun isPathVariable(input: String): Boolean {
        return input.matches(PATH_VARIABLE_PATTERN.toRegex())
    }

    fun isCamelCase(input: String): Boolean {
        return input.matches(CAMEL_CASE_PATTERN.toRegex())
    }

    fun isPascalCase(input: String): Boolean {
        return input.matches(PASCAL_CASE_PATTERN.toRegex())
    }

    fun isHyphenatedCamelCase(input: String): Boolean {
        return input.matches(HYPHENATED_CAMEL_CASE_PATTERN.toRegex())
    }

    fun isHyphenatedPascalCase(input: String): Boolean {
        return input.matches(HYPHENATED_PASCAL_CASE_PATTERN.toRegex())
    }

    fun isSnakeCase(input: String): Boolean {
        return input.matches(SNAKE_CASE_PATTERN.toRegex())
    }

    fun isKebabCase(input: String): Boolean {
        return input.matches(KEBAB_CASE_PATTERN.toRegex())
    }

    fun isHyphenated(input: String): Boolean {
        return input.matches("^[A-Za-z0-9.]+(-[A-Za-z0-9.]+)*$".toRegex())
    }

    fun hasVersionInUrl(input: String): Boolean {
        return input.matches(VERSION_IN_URL_PATTERN.toRegex())
    }

    fun isVersion(input: String): Boolean {
        return input.matches(GENERIC_VERSION_PATTERN.toRegex())
    }

    fun isApplicationJsonOrProblemJson(mediaType: String): Boolean {
        return mediaType.matches(PATTERN_APPLICATION_PROBLEM_JSON.toRegex())
    }

    fun isCustomMediaTypeWithVersioning(mediaType: String): Boolean {
        return mediaType.matches(PATTERN_CUSTOM_WITH_VERSIONING.toRegex())
    }
}
