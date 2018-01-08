package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.ArrayModel
import io.swagger.models.Model
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.RefProperty
import io.swagger.parser.ResolverCache

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "CollectionsReturnArrays",
        severity = Severity.MUST,
        title = "Collection Resources Return Arrays"
)
class CollectionsReturnArrays : AbstractRule() {
    val description = "Collection resources return arrays so that they can be acted upon easily"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.collections()
                    .flatMap { (pattern, path) ->
                        path.get?.responses.orEmpty()
                                .filterKeys { Integer.parseInt(it) in 200..299 }
                                .filterValues { !isArrayResponse(it, swagger) }
                                .map { (code, response) ->
                                    "paths $pattern GET responses $code schema type: expected array but found ${response?.schema?.type}"
                                }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    private fun isArrayResponse(response: Response, swagger: Swagger): Boolean {
        val schema = response.schema ?: return false

        if (schema is ArrayProperty) {
            return true
        }

        if (schema is RefProperty) {
            val resolver = ResolverCache(swagger, null, null)
            return resolver.loadRef(schema.`$ref`, schema.refFormat, Model::class.java) is ArrayModel
        }

        return false
    }
}
