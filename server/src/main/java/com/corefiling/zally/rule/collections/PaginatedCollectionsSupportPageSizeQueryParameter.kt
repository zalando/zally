package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.QueryParameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaginatedCollectionsSupportPageSizeQueryParameter(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Paginated Resources Support 'pageSize' Query Parameter"
    override val violationType = ViolationType.SHOULD
    override val description = "Paginated resources support a 'pageSize' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    @Check
    fun validate(swagger: Swagger): Violation? = swagger.collections()
            .map { (pattern, path) ->
                when {
                    hasPageSizeQueryParam(path.get) -> null
                    else -> "paths $pattern GET parameters: does not include a valid pageSize query parameter"
                }
            }
            .filterNotNull()
            .takeIf(List<String>::isNotEmpty)
            ?.let { it: List<String> ->
                Violation(this, title, description, violationType, it)
            }

    private fun hasPageSizeQueryParam(op: Operation?): Boolean =
            op?.parameters?.find { isPageSizeQueryParam(it) } != null

    private fun isPageSizeQueryParam(param: Parameter): Boolean {
        if (param !is QueryParameter) {
            return false
        }
        return when {
            param.name != "pageSize" -> false
            param.type != "integer" -> false
            param.format != "int32" -> false
            param.minimum != BigDecimal(1) -> false
            else -> true
        }
    }
}