package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import de.zalando.zally.util.PatternUtil.isCamelCase
import io.swagger.models.HttpMethod
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PutOperationsRequireBodyParameter(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "PUT Operations Require Body Parameter"
    override val violationType = ViolationType.MUST
    override val description = "Put operations are meaningless without a body parameter"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty().flatMap { (pattern, path) ->
                path.operationMap.orEmpty().filterKeys { it==HttpMethod.PUT }.map { (method, op) ->
                    val body = op.parameters.orEmpty().filter { it is BodyParameter }.firstOrNull()
                    validate("$pattern $method body parameter", body)
                }
            }.filterNotNull().takeIf { it.isNotEmpty() }?.let { Violation(this, title, description, violationType, it) }

    fun validate(location: String, parameter: Parameter?): String? {
        return when {
            parameter==null -> "$location is missing!"
            !parameter.required -> "$location '${parameter.name}' is optional!"
            else -> null
        }
    }
}