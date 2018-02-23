package com.corefiling.pdds.zally.rule.collections

import com.corefiling.pdds.zally.extensions.validateOperation
import com.corefiling.pdds.zally.extensions.validatePath
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import java.math.BigDecimal

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PaginatedCollectionsSupportPageNumberQueryParameter",
        severity = Severity.SHOULD,
        title = "Paginated Resources Support 'pageNumber' Query Parameter"
)
class PaginatedCollectionsSupportPageNumberQueryParameter {
    val description = "Paginated resources support a 'pageNumber' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.validateOperation(description) { pattern, path, method, _ ->
                "parameters: does not include a valid pageNumber query parameter"
                        .onlyIf(method == HttpMethod.GET
                                && swagger.isCollection(pattern, path)
                                && !hasPageNumberQueryParam(path.get))
            }

    private fun hasPageNumberQueryParam(op: Operation?): Boolean =
            op?.parameters?.find { isPageNumberQueryParam(it) } != null

    private fun isPageNumberQueryParam(param: Parameter): Boolean {
        if (param !is QueryParameter) {
            return false
        }
        return when {
            param.name != "pageNumber" -> false
            param.type != "integer" -> false
            param.format != "int32" -> false
            param.minimum != BigDecimal(1) -> false
            !param.required -> false
            else -> true
        }
    }
}
