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
class PaginatedCollectionsReturnXTotalPagesHeader(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Paginated Resources Return X-Total-Pages Header"
    override val violationType = ViolationType.SHOULD
    override val description = "Paginated resources return the X-Total-Pages header " +
            "with type:integer and format:int32 so that clients can easily iterate over the collection."

    override fun validate(swagger: Swagger): Violation? = swagger.collections()
            .flatMap { (pattern, path) ->
                path.get?.responses.orEmpty()
                        .filterKeys { Integer.parseInt(it) in 200..299 }
                        .filterValues { !hasXTotalPagesHeader(it) }
                        .map { (code, _) ->
                            "paths $pattern GET responses $code headers: does not include an int32 format integer X-Total-Pages header"
                        }
            }
            .takeIf(List<String>::isNotEmpty)
            ?.let { it: List<String> ->
                Violation(this, title, description, violationType, url, it)
            }

    private fun hasXTotalPagesHeader(response: Response?): Boolean {
        val header = response?.headers?.get("X-Total-Pages") ?: return false
        return when {
            header.type != "integer" -> false
            header.format != "int32" -> false
            else -> true
        }
    }
}
