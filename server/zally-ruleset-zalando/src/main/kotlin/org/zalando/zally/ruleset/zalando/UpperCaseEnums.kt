package org.zalando.zally.ruleset.zalando

import io.swagger.v3.oas.models.media.Schema
import org.zalando.zally.core.util.extensibleEnum
import org.zalando.zally.core.util.getAllProperties
import org.zalando.zally.core.util.getAllSchemas
import org.zalando.zally.core.util.isEnum
import org.zalando.zally.core.util.isExtensibleEnum
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "240",
    severity = Severity.SHOULD,
    title = "Declare enum values using UPPER_SNAKE_CASE format"
)
class UpperCaseEnums {

    private val pattern = "[A-Z_0-9]*".toRegex()

    private val description = "Enum value(s) should use UPPER_SNAKE_CASE format"

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        validatePrimitiveSchemas(context) + validateAllProperties(context)

    private fun validateAllProperties(context: Context): List<Violation> = context.api.getAllProperties()
        .filter { it.value.type == "string" }
        .filter { it.value.isExtensibleEnum() || it.value.isEnum() }
        .flatMap { (_, property) ->
            validateEnum(property, context)
        }

    private fun validatePrimitiveSchemas(context: Context): List<Violation> = context.api.getAllSchemas()
        .filter { it.type == "string" }
        .filter { it.isExtensibleEnum() || it.isEnum() }
        .flatMap { validateEnum(it, context) }

    private fun validateEnum(scheme: Schema<Any>, context: Context): List<Violation> {
        val enumValues = if (scheme.isExtensibleEnum()) {
            scheme.extensibleEnum()
        } else {
            scheme.enum
        }

        return enumValues.filterNotNull().filterNot { it is String }.map {
            context.violation(
                "${scheme.name}: Enum value type ${it.javaClass.simpleName} is not valid. The expected type is string",
                scheme
            )
        } +
            enumValues.filter { it is String }.map { it.toString() }.filterNot { pattern.matches(it) }.map {
                context.violation("${scheme.name}: $description. Incorrect value: $it", scheme)
            }
    }
}
