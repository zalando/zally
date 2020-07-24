package org.zalando.zally.ruleset.sbb

import org.zalando.zally.core.util.getAllSchemas
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.ruleset.zalando.util.WordUtil.isPlural

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#array-names-should-be-pluralized",
    severity = Severity.SHOULD,
    title = "Array names should be pluralized"
)
class PluralizeNamesForArraysRule {

    val description = "Array property name appears to be singular"

    @Check(severity = Severity.SHOULD)
    fun checkArrayPropertyNamesArePlural(context: Context): List<Violation> =
        context.api.getAllSchemas()
            .flatMap { it.properties.orEmpty().entries }
            .filter { "array" == it.value.type }
            .filterNot { isPlural(it.key) }
            .map { context.violation("$description: ${it.key}", it.value) }
}
