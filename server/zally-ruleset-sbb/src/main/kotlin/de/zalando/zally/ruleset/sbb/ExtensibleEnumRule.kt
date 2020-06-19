package de.zalando.zally.ruleset.sbb

import de.zalando.zally.core.util.getAllTransitiveSchemas
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "107",
    severity = Severity.MAY,
    title = "Prefer Compatible Extensions"
)
class ExtensibleEnumRule {

    val description = "Property is not an extensible enum (use `x-extensible-enum` instead)"

    @Check(severity = Severity.MAY)
    fun checkForEnums(context: Context): List<Violation> =
        context.api.getAllTransitiveSchemas()
            .filter { it.enum != null && it.enum.isNotEmpty() }
            .map { context.violation(description, it) }
}
