package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.extensions.isQuery

/**
 * Lint for snake case for query params
 */
@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "130",
        severity = Severity.MUST,
        title = "Use snake_case (never camelCase) for Query Parameters"
)
class SnakeCaseForQueryParamsRule {

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        val result = adapter.openAPI.paths.orEmpty().flatMap { (path, pathObject) ->
            pathObject.readOperationsMap().orEmpty().flatMap { (verb, operation) ->
                val badParams = operation.parameters.filter { it.isQuery() && !PatternUtil.isSnakeCase(it.name) }
                if (badParams.isNotEmpty()) listOf("$path $verb" to badParams) else emptyList()
            }
        }
        return if (result.isNotEmpty()) {
            val (paths, params) = result.unzip()
            val description = "Parameters that are not in snake_case: " + params.flatten().map { it.name }.toSet().joinToString(",")
            Violation(description, paths)
        } else null
    }
}
