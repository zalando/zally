package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "AtMostOneBodyParameter",
        severity = Severity.MUST,
        title = "At Most One Body Parameter"
)
class AtMostOneBodyParameter : AbstractRule() {
    val description = "No more than one body parameter can be associated with an operation"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().map { (method, op) ->
                            val bodies = op.parameters.orEmpty().filter { it is BodyParameter }
                            validate("$pattern $method", bodies)
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    fun validate(location: String, parameters: List<Parameter>): String? {
        return when {
            parameters.size > 1 -> "$location has multiple body parameters ${parameters.map(Parameter::getName)}"
            else -> null
        }
    }
}