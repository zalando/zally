package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.util.getAllTransitiveSchemas
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "107",
    severity = Severity.SHOULD,
    title = "Prefer Compatible Extensions"
)
class ExtensibleEnumRule {

    val description = "Property is not an extensible enum (use `x-extensible-enum` instead)"

    @Check(severity = Severity.SHOULD)
    fun checkForEnums(context: Context): List<Violation> =
        context.api.getAllTransitiveSchemas()
            .filter { it.enum != null && it.enum.isNotEmpty() }
            .map { context.violation(description, it) }
}
