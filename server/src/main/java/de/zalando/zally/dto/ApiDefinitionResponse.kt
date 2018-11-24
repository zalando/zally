package de.zalando.zally.dto

import java.util.UUID

data class ApiDefinitionResponse(
    val externalId: UUID? = null,
    var message: String? = null,
    var violations: List<ViolationDTO>? = null,
    var violationsCount: Map<String, Int>? = null
)
