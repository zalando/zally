package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.ArrayModel
import io.swagger.models.Model
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

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, path ->
            if (path.get!=null) {

                path.get.responses?.forEach { code, response ->
                    if (Integer.parseInt(code) < 300) {
                        var array = response.schema is ArrayProperty

                        if (response.schema is RefProperty) {
                            val ref = response.schema as RefProperty
                            val resolver = ResolverCache(swagger, null, null)
                            array = resolver.loadRef(ref.`$ref`, ref.refFormat, Model::class.java) is ArrayModel
                        }

                        if (!array) {
                            failures.add(pattern)
                        }
                    }
                }
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }
}
