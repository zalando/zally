package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.extensions.allSchemas
import de.zalando.zally.util.extensions.isObject
import de.zalando.zally.util.extensions.producesJson

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

        val paths = adapter.openAPI.paths.orEmpty().flatMap { (key, value) ->

            value
                    .readOperationsMap()
                    .orEmpty()
                    .filter { (_, it) -> it.producesJson }
                    .flatMap { (method, operation) ->
                        operation.responses.orEmpty().filter { (code, response) ->
                            isSuccess(code) && !response.content.allSchemas().filter { it.isObject() }.isNotEmpty()
                        }.map { (code, _) ->
                            "$key $method $code"
                        }
                    }
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }


    private fun isSuccess(codeString: String) = codeString.toIntOrNull() in 200..299
}
