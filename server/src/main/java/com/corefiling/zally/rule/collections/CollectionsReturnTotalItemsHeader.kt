package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Response
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CollectionsReturnTotalItemsHeader(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Collection Resources Return Total-Items Header"
    override val violationType = ViolationType.SHOULD
    override val description = "Collection resources return the Total-Items header " +
            "with type:integer and format:int32 so that clients can easily access the total"

    override fun validate(swagger: Swagger): Violation? = swagger.collections()
            .flatMap { (pattern, path) ->
                path.get?.responses.orEmpty()
                        .filterKeys { Integer.parseInt(it) in 200..299 }
                        .filterValues { !hasTotalItemsHeader(it) }
                        .map { (code, _) ->
                            "paths $pattern GET responses $code headers: does not include an int32 format integer Total-Items header"
                        }
            }
            .takeIf(List<String>::isNotEmpty)
            ?.let { it: List<String> ->
                Violation(this, title, description, violationType, url, it)
            }

    private fun hasTotalItemsHeader(response: Response?): Boolean {
        val header = response?.headers?.get("Total-Items") ?: return false
        return when {
            header.type != "integer" -> false
            header.format != "int32" -> false
            else -> true
        }
    }
}
