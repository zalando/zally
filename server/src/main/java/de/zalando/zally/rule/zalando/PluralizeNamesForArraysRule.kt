package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.WordUtil.isPlural
import de.zalando.zally.core.util.getAllSchemas

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "120",
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
