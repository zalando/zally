package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import de.zalando.zally.util.PatternUtil.isCamelCase
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LowerCamelCaseParameterNames(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Lower Camel Case Parameter Names"
    override val violationType = ViolationType.SHOULD
    override val description = "Query and path parameters should be named in lowerCamelCase style"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty().flatMap { (pattern, path) ->
                path.operationMap.orEmpty().flatMap { (method, op) ->
                    op.parameters.orEmpty()
                            .map { validate(it, "$pattern $method ${it.`in`} parameter ${it.name}") }
                            .filterNotNull()
                }
            }.takeIf { it.isNotEmpty() }?.let { Violation(this, title, description, violationType, it) }

    fun validate(parameter: Parameter, location: String): String? {
        val name = parameter.name
        return if (isCamelCase(name)) null else {
            when (parameter) {
                is PathParameter -> "$location is not lowerCamelCase"
                is QueryParameter -> "$location is not lowerCamelCase"
                else -> null
            }
        }
    }
}