package de.zalando.zally.rule

import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Violation

data class Result(
        val rule: Rule,
        val title: String,
        val description: String,
        val violationType: Severity,
        val paths: List<String>
) {
    fun toViolation(): Violation = Violation(description, paths)
}
