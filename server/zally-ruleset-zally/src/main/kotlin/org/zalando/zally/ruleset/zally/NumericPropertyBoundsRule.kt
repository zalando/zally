package org.zalando.zally.ruleset.zally

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.core.util.getAllProperties
import java.math.BigDecimal

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "S006",
    severity = Severity.SHOULD,
    title = "Define bounds for numeric properties"
)
class NumericPropertyBoundsRule {

    @Check(severity = Severity.SHOULD)
    fun checkNumericBounds(context: Context): List<Violation> =
        context.api
            .getAllProperties()
            .filterValues { schema -> schema.type in arrayOf("integer", "number") }
            .flatMap { (_, schema) ->
                context.violationsIfNull(schema.minimum, "No minimum defined", schema) +
                context.violationsIfNull(schema.maximum, "No maximum defined", schema)
            }

    private fun Context.violationsIfNull(value: BigDecimal?, description: String, location: Any): List<Violation> = when {
        value != null -> emptyList()
        else -> this.violations(description, location)
    }
}
