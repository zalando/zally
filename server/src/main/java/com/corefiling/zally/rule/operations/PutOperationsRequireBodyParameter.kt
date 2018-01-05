package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PutOperationsRequireBodyParameter",
        severity = Severity.MUST,
        title = "PUT Operations Require Body Parameter"
)
class PutOperationsRequireBodyParameter : AbstractRule() {
    val description = "Put operations are meaningless without a body parameter"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty()
                                .filterKeys { it == HttpMethod.PUT }
                                .map { (method, op) ->
                                    val body = op.parameters.orEmpty().firstOrNull { it is BodyParameter }
                                    validate("$pattern $method body parameter", body)
                                }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    fun validate(location: String, parameter: Parameter?): String? {
        return when {
            parameter == null -> "$location is missing!"
            !parameter.required -> "$location '${parameter.name}' is optional!"
            else -> null
        }
    }
}