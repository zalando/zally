package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType

interface Rule {

    val ruleSet: RuleSet
    val title: String
    val violationType: ViolationType
    val url: String?
    val code: String
    val guidelinesCode: String
    val name: String

}
