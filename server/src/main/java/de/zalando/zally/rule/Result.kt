package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

data class Result(
    val ruleSet: RuleSet,
    val rule: Rule,
    val description: String,
    val violationType: Severity,
    val pointer: JsonPointer
) {
    fun toViolation(): Violation = Violation(description, pointer)
}
