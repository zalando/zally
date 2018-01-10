package com.corefiling.pdds.zally.rule.collections

import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Response
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PaginatedCollectionsReturnTotalPagesHeader",
        severity = Severity.SHOULD,
        title = "Paginated Resources Return Total-Pages Header"
)
class PaginatedCollectionsReturnTotalPagesHeader : AbstractRule() {
    val description = "Paginated resources return the Total-Pages header " +
            "with type:integer and format:int32 so that clients can easily iterate over the collection."

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.collections()
                    .flatMap { (pattern, path) ->
                        path.get?.responses.orEmpty()
                                .filterKeys { Integer.parseInt(it) in 200..299 }
                                .filterValues { !hasTotalPagesHeader(it) }
                                .map { (code, _) ->
                                    "paths $pattern GET responses $code headers: does not include an int32 format integer Total-Pages header"
                                }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    private fun hasTotalPagesHeader(response: Response?): Boolean {
        val header = response?.headers?.get("Total-Pages") ?: return false
        return when {
            header.type != "integer" -> false
            header.format != "int32" -> false
            else -> true
        }
    }
}
