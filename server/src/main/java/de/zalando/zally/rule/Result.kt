package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

data class Result(
        val ruleSet: RuleSet,
        val rule: Rule,
        val description: String,
        val violationType: Severity,
        val paths: List<String>
) {
    fun toViolation(): Violation = Violation(description, paths)
}
