package org.zalando.zally.ruleset.sbb

import com.typesafe.config.Config
import io.swagger.v3.oas.models.media.Schema
import org.zalando.zally.core.util.getAllProperties
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#name-datetime-properties-using-the-at-suffix",
    severity = Severity.SHOULD,
    title = "Name date/time properties using the \"_at\" suffix"
)
class DateTimePropertiesSuffixRule(rulesConfig: Config) {

    private val patterns: Set<Regex> = rulesConfig
        .getStringList("${DateTimePropertiesSuffixRule::class.java.simpleName}.patterns")
        .map { Regex(it) }
        .toSet()

    private val propertyFormats = setOf("date", "date-time")

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> {
        return context.api.getAllProperties().map {
            val schema = it.value
            val result = checkProperty(it.key, schema)
            result?.let { context.violation(result, schema) }
        }.filterNotNull()
    }

    private fun checkProperty(name: String, schema: Schema<Any>): String? {
        if (schema.type == "string" && schema.format in propertyFormats) {
            patterns.find { it.matches(name) } ?: return generateMessage(name, schema.type, schema.format)
            return null
        }
        return null
    }

    internal fun generateMessage(name: String, type: String, format: String) =
        """Property "$name" of type "$type" and format "$format" should match one of the patterns ${patterns.map { it.toString() }}""""
}
