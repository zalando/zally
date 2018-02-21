package com.corefiling.pdds.zally.rule.operations

import com.corefiling.pdds.zally.extensions.validateOperation
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
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
class AtMostOneBodyParameter {
    val description = "No more than one body parameter can be associated with an operation"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.validateOperation(description) { _, _, _, op ->
                val bodies = op.parameters.orEmpty().filter { it is BodyParameter }
                when {
                    bodies.size > 1 -> "has multiple body parameters ${bodies.map(Parameter::getName)}"
                    else -> null
                }
            }
}