package org.zalando.zally.dto

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include
import org.zalando.zally.rule.api.Severity

data class ViolationDTO(
    val title: String? = null,
    val description: String? = null,
    val violationType: Severity? = null,
    val ruleLink: String? = null,
    @JsonInclude(Include.NON_NULL) @Deprecated("Use `pointer` instead.") val paths: List<String> = emptyList(),
    @JsonInclude(Include.NON_NULL) val pointer: String? = null,
    val startLine: Int? = null,
    val endLine: Int? = null
)
