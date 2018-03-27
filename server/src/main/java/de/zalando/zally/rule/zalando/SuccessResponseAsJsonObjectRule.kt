package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.ComposedModel
import io.swagger.models.ModelImpl
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "110",
        severity = Severity.MUST,
        title = "Response As JSON Object"
)
class SuccessResponseAsJsonObjectRule {
    private val description = "Always Return JSON Objects As Top-Level Data Structures To Support Extensibility"

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger!!
            val paths = swagger.paths.orEmpty().flatMap { (key, value) ->
                value.operationMap.orEmpty().filter { it.value.producesJson }.flatMap { (method, operation) ->
                    operation.responses.orEmpty().filter { (code, response) ->
                        isSuccess(code) && !response.schema.isObject(swagger)
                    }.map { (code, _) ->
                        "$key $method $code"
                    }
                }
            }
            return if (paths.isNotEmpty()) Violation(description, paths) else null
        }
        return Violation.UNSUPPORTED_API_VERSION
    }

    private val Operation.producesJson get() = produces == null || produces.isEmpty() || produces.any { "json" in it }

    private fun isSuccess(codeString: String) = codeString.toIntOrNull() in 200..299

    private fun Property?.isObject(swagger: Swagger) =
            when {
                this == null -> true
                type == "object" -> true
                this is RefProperty -> {
                    val model = swagger.definitions.orEmpty()[simpleRef]
                    (model is ModelImpl && model.type == "object") || model is ComposedModel
                }
                else -> false
            }
}
