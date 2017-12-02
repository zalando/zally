package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import org.springframework.beans.factory.annotation.Autowired

class QueryParameterCollectionFormatRule(@Autowired ruleSet: ZalandoRuleSet) : AbstractRule(ruleSet) {

    override val title = "Explicitly define the Collection Format of Query Parameters"
    override val violationType = ViolationType.SHOULD
    override val id = "154"
    val formatsAllowed = listOf("csv", "multi")
    val violationDescription = "CollectionFormat should be one of: $formatsAllowed"

    @Check(severity = ViolationType.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        fun Collection<Parameter>?.extractInvalidQueryParam(path: String) =
            orEmpty().filterIsInstance<QueryParameter>()
                .filter { it.`type` == "array" && it.collectionFormat !in formatsAllowed }
                .map { path to it.name }

        val fromParams = swagger.parameters.orEmpty().values.extractInvalidQueryParam("parameters")
        val fromPaths = swagger.paths.orEmpty().entries.flatMap { (name, path) ->
            path.parameters.extractInvalidQueryParam(name) + path.operations.flatMap { operation ->
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
