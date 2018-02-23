package com.corefiling.pdds.zally.rule.collections

import com.corefiling.pdds.zally.extensions.validateResponse
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Response
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PaginatedCollectionsReturnTotalPagesHeader",
        severity = Severity.SHOULD,
        title = "Paginated Resources Return Total-Pages Header"
)
class PaginatedCollectionsReturnTotalPagesHeader {
    val description = "Paginated resources return the Total-Pages header " +
            "with type:integer and format:int32 so that clients can easily iterate over the collection."

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.validateResponse(description) { pattern, path, method, _, status, response ->
                "headers: does not include an int32 format integer Total-Pages header"
                        .onlyIf(method == HttpMethod.GET
                                && swagger.isCollection(pattern, path)
                                && Integer.parseInt(status) in 200..299
                                && !hasTotalPagesHeader(response))
            }

    private fun hasTotalPagesHeader(response: Response?): Boolean {
        val header = response?.headers?.get("Total-Pages") ?: return false
        return when {
            header.type != "integer" -> false
            header.format != "int32" -> false
            else -> true
        }
    }
}
