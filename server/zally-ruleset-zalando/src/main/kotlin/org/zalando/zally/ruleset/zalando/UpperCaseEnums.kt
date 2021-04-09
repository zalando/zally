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

    private val description = "Enum value should use UPPER_SNAKE_CASE format"

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.api.getAllSchemas()
            .filter { it.type == "string" }
            .filter { it.isExtensibleEnum() || it.isEnum() }
            .flatMap {
                if (it.isExtensibleEnum()) {
                    validateExtensibleEnum(it)
                } else {
                    validateEnum(it)
                }.map { enumValue -> context.violation("$description: $enumValue", it) }
            } +
            context.api.getAllProperties()
                .filter { it.value.type == "string" }
                .filter { it.value.isExtensibleEnum() || it.value.isEnum() }
                .flatMap { (propName, property) ->
                    if (property.isExtensibleEnum()) {
                        validateExtensibleEnum(property)
                    } else {
                        validateEnum(property)
                    }.map {
                        context.violation("$description: $propName", it)
                    }
                }

    private fun validateExtensibleEnum(property: Schema<Any>): List<String> =
        property.extensibleEnum().filter { !pattern.matches(it) }

    private fun validateEnum(property: Schema<Any>): List<String> =
        property.enum.map { it.toString() }.filter { !pattern.matches(it) }
}
