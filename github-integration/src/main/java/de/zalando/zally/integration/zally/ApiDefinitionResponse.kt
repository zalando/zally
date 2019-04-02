package de.zalando.zally.integration.zally

data class ApiDefinitionResponse(
    val message: String? = null,
    val violations: List<Violation> = emptyList(),
    val violationsCount: Map<String, Int>? = emptyMap() // null values can actually be stored in the DB
)
