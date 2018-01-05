package com.corefiling.zally.rule.naming

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isCamelCase
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "LowerCamelCaseParameterNames",
        severity = Severity.SHOULD,
        title = "Lower Camel Case Parameter Names"
)
class LowerCamelCaseParameterNames : AbstractRule() {
    val description = "Query and path parameters should be named in lowerCamelCase style"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().flatMap { (method, op) ->
                            op.parameters.orEmpty().mapNotNull { validate(it, "$pattern $method ${it.`in`} parameter ${it.name}") }
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

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