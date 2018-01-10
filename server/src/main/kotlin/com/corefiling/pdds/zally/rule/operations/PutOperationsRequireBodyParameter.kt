package com.corefiling.pdds.zally.rule.operations

import com.corefiling.pdds.zally.extensions.validateOperation
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter

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
            swagger.validateOperation(description) { _, _, method, op ->
                val body = op.parameters.orEmpty().firstOrNull { it is BodyParameter }
                when {
                    method != HttpMethod.PUT -> null
                    body == null -> "body parameter is missing!"
                    !body.required -> "body parameter '${body.name}' is optional!"
                    else -> null
                }
            }
}