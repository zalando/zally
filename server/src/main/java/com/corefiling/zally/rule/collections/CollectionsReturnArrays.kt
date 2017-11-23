package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.ArrayModel
import io.swagger.models.Model
import io.swagger.models.Response
import io.swagger.models.Swagger
import io.swagger.models.properties.ArrayProperty
import io.swagger.models.properties.RefProperty
import io.swagger.parser.ResolverCache
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CollectionsReturnArrays(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Collection Resources Return Arrays"
    override val violationType = ViolationType.MUST
    override val description = "Collection resources return arrays so that they can be acted upon easily"

    override fun validate(swagger: Swagger): Violation? = swagger.collections()
            .flatMap { (pattern, path) ->
                path.get?.responses.orEmpty()
                        .filterKeys { Integer.parseInt(it) in 200..299 }
                        .filterValues { !isArrayResponse(it, swagger) }
                        .map { (code, response) ->
                            "paths $pattern GET responses $code schema type: expected array but found ${response?.schema?.type}"
                        }
            }
            .takeIf(List<String>::isNotEmpty)?.let { it: List<String> ->
                Violation(this, title, description, violationType, url, it)
            }

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
