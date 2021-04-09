package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.util.getAllProperties
import org.zalando.zally.core.util.getAllSchemas
import org.zalando.zally.core.util.isExtensibleEnum
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "125",
    severity = Severity.SHOULD,
    title = "Represent enumerations as strings"
)
class EnumValueTypeRule {

    private val description = "Enumeration value should have \"type = string\""

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.api.getAllSchemas()
            .filter { it.isExtensibleEnum() }
            .filter { it.type != "string" }
            .map {
                context.violation("$description: ${it.name}", it)
            } +
            context.api.getAllProperties()
                .filter { it.value.isExtensibleEnum() }
                .filter { it.value.type != "string" }
                .map {
                    context.violation("$description: ${it.key}", it.value)
                }
}
