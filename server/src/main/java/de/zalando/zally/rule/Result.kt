package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import java.util.Arrays.asList

data class Result(
    val ruleSet: RuleSet,
    val rule: Rule,
    val description: String,
    val violationType: Severity,
    @Deprecated("Use `pointer` instead.") val paths: List<String>,
    val pointer: String? = if (paths.size == 1) paths[0] else null
) {
    constructor(
        ruleSet: RuleSet,
        rule: Rule,
        description: String,
        violationType: Severity,
        vararg paths: String
    ) : this(ruleSet, rule, description, violationType, asList(*paths))

    fun toViolation(): Violation = Violation(description, paths)
}
