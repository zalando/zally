package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaginatedCollectionsSupportPageNumberQueryParameter(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Paginated Resources Support 'pageNumber' Query Parameter"
    override val violationType = ViolationType.SHOULD
    override val description = "Paginated resources support a 'pageNumber' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    override fun validate(swagger: Swagger): Violation? = swagger.collections()
                .map { (pattern, path) ->
                    when {
                        (hasPageNumberQueryParam(path.get)) -> null
                        else -> "paths $pattern GET parameters: does not include a valid pageNumber query parameter"
                    }
                }
                .filterNotNull()
                .takeIf(List<String>::isNotEmpty)
                ?.let { it: List<String> ->
                    Violation(this, title, description, violationType, url, it)
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