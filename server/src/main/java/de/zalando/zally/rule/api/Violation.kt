package de.zalando.zally.rule.api

data class Violation(
    val description: String,
    val paths: List<String>
) {

    companion object {
        val UNSUPPORTED_API_VERSION = Violation("Rule does not support provided api version", emptyList())
    }
}
