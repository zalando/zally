package de.zalando.zally.dto

import de.zalando.zally.rule.api.Severity

data class ViolationDTO(

        var title: String? = null,
        var description: String? = null,
        var violationType: Severity? = null,
        var ruleLink: String? = null,
        var paths: List<String>? = null
)
