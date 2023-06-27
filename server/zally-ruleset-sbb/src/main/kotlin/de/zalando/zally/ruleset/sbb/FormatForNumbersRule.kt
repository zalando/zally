package de.zalando.zally.ruleset.sbb

import com.typesafe.config.Config
import de.zalando.zally.core.util.getAllSchemas
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "171",
    severity = Severity.MUST,
    title = "Define Format for Type Number and Integer"
)
class FormatForNumbersRule(rulesConfig: Config) {
    private val description = """Numeric properties must have valid format specified"""

    private val numberTypes = listOf("integer", "number")
    @Suppress("UNCHECKED_CAST")
    private val type2format = rulesConfig.getConfig("${javaClass.simpleName}.formats").entrySet()
        .map { (key, config) -> key to config.unwrapped() as List<String> }.toMap()

    @Check(severity = Severity.MUST)
    fun checkNumberFormat(context: Context): List<Violation> =
        context.api.getAllSchemas()
            .flatMap { it.properties.orEmpty().values }
            .filter { it.type in numberTypes }
            .filter { it.format == null || !isValid(it.type, it.format) }
            .map { context.violation(description, it) }

    private fun isValid(type: String?, format: String): Boolean = type2format[type]?.let { format in it } ?: true
}
