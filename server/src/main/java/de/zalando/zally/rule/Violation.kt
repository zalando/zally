package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.api.Rule

data class Violation(

    val rule: Rule,
    val title: String,
    val description: String,
    val violationType: ViolationType,
    val paths: List<String>
)
