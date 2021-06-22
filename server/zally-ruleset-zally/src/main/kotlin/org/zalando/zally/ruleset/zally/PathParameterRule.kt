package org.zalando.zally.ruleset.zally

import org.zalando.zally.core.util.getAllParameters
import org.zalando.zally.core.util.isInPath
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "Z001",
    severity = Severity.MUST,
    title = "Path parameters validation"
)
class PathParameterRule {

    companion object {
        const val REQUIRED_ATTRIBUTE_ERROR_MESSAGE =
            "Parameter with location \"path\" must have an attribute \"required=true\" set"

        fun requiredSchemaOrContentErrorMessage(parameterName: String) =
            "Parameter $parameterName should have either \"schema\" or \"content\" defined"

        fun contentMapStructureErrorMessage(parameterName: String) =
            "Parameter $parameterName: \"content\" property should have exactly one entry"
    }

    @Check(severity = Severity.MUST)
    fun checkRequiredPathAttribute(context: Context): List<Violation> =
        context.api.getAllParameters().map { entry -> entry.value }
            .filter { parameter ->
                parameter.isInPath() && !parameter.required
            }
            .map {
                context.violation(REQUIRED_ATTRIBUTE_ERROR_MESSAGE, it)
            }

    @Check(severity = Severity.MUST)
    fun checkSchemaOrContentProperty(context: Context): List<Violation> {
        if (!context.isOpenAPI3()) {
            emptyList<Violation>()
        }
        return context.api
            .getAllParameters()
            .filterValues { it.schema == null && it.content == null }
            .map { (_, parameter) ->
                context.violation(requiredSchemaOrContentErrorMessage(parameter.name), parameter)
            }
    }

    @Check(severity = Severity.MUST)
    fun validateParameterContentMapStructure(context: Context): List<Violation> {
        if (!context.isOpenAPI3()) {
            emptyList<Violation>()
        }
        return context.api.getAllParameters()
            .filterValues {
                val contentMap = it.content.orEmpty()
                contentMap.isEmpty() || contentMap.size > 1
            }
            .map { (_, parameter) ->
                context.violation(contentMapStructureErrorMessage(parameter.name), parameter)
            }
    }
}
