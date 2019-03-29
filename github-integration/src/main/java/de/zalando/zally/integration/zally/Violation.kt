package de.zalando.zally.integration.zally

data class Violation(
    val title: String? = null,
    val description: String? = null,
    val violationType: ViolationType? = null,
    val ruleLink: String? = null,
    val paths: List<String>? = emptyList() // null values can actually be stored in the DB

)
