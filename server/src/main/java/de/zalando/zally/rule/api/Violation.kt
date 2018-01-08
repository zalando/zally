package de.zalando.zally.rule.api

data class Violation(
        val description: String,
        val paths: List<String>
)
