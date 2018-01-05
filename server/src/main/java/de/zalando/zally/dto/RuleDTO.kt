package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonProperty
import de.zalando.zally.rule.api.Severity

data class RuleDTO(

        var title: String? = null,
        var type: Severity? = null,
        var url: String? = null,
        var code: String? = null,
        @JsonProperty("is_active") var active: Boolean? = null
)
