package de.zalando.zally.rule.api

import java.util.Arrays.asList

data class Violation(
    val description: String,
    @Deprecated("Use `pointer` instead.") val paths: List<String>,
    val pointer: String? = null
) {
    constructor(description: String, vararg paths: String) : this(description, asList(*paths))
}
