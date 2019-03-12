package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonProperty
import de.zalando.zally.rule.api.Severity

data class RuleDTO(
    val title: String? = null,
    val type: Severity? = null,
    val url: String? = null,
    val code: String? = null,
    @JsonProperty("is_active") val active: Boolean? = null
)
