package de.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import de.zalando.zally.rule.api.Severity

data class ViolationDTO(
    var title: String? = null,
    var description: String? = null,
    var violationType: Severity? = null,
    var ruleLink: String? = null,
    @JsonInclude(Include.NON_NULL) @Deprecated("Use `pointer` instead.") var paths: List<String>? = null,
    @JsonInclude(Include.NON_NULL) var pointer: String? = null
)
