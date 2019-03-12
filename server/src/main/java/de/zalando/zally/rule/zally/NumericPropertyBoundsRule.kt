package de.zalando.zally.rule.zally

import com.typesafe.config.Config
import de.zalando.zally.rule.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllProperties
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "S006",
    severity = Severity.SHOULD,
    title = "Define bounds for numeric properties"
)
class NumericPropertyBoundsRule(@Autowired config: Config) {

    private val checker = CaseChecker.load(config)

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
