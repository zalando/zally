package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.parameters.QueryParameter

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "154",
        severity = Severity.SHOULD,
        title = "Explicitly define the Collection Format of Query Parameters"
)
class QueryParameterCollectionFormatRule {
    private val formatsAllowed = listOf("csv", "multi")
    private val violationDescription = "CollectionFormat should be one of: $formatsAllowed"

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {

        fun Collection<Parameter>?.extractInvalidQueryParam(path: String) =
                orEmpty()
                        .filterIsInstance<QueryParameter>()
                        .filter { it.schema.type == "array" && it.schema.format !in formatsAllowed }
                        .map { path to it.name }

        val fromParams = adapter.openAPI.components.parameters.orEmpty().values.extractInvalidQueryParam("parameters")
        val fromPaths = adapter.openAPI.paths.orEmpty().entries.flatMap { (name, path) ->
            path.parameters.extractInvalidQueryParam(name) + path.readOperations().flatMap { operation ->
                operation.parameters.extractInvalidQueryParam(name)
            }
        }

        val allHeaders = fromParams + fromPaths
        val paths = allHeaders
                .map { "${it.first} ${it.second}" }
                .distinct()

        return if (paths.isNotEmpty()) createViolation(paths) else null
    }

    fun createViolation(paths: List<String>): Violation {
        return Violation(violationDescription, paths)
    }
}
