package org.zalando.zally.dto

import java.util.UUID

data class ApiDefinitionResponse(
    val externalId: UUID? = null,
    val message: String? = null,
    val score: Float,
    val violations: List<ViolationDTO> = emptyList(),
    val violationsCount: Map<String, Int> = emptyMap(),
    val apiDefinition: String?
)
