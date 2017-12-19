package com.corefiling.zally.rule.naming

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Operation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class MatchingSummaryAndOperationIdNames(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Matching Summary and OperationId"
    override val violationType = ViolationType.SHOULD
    override val description = "OperationId should be the lowerCamelCase version of the summary"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().map { (method, op) ->
                            validate("$pattern $method", op)
                        }.filterNotNull()
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }

    fun validate(location: String, op: Operation): String? {
        return when {
            op.operationId == null -> "$location has no operationId!"
            op.summary == null -> "$location has no summary!"
            op.operationId != lowerCamelCase(op.summary) -> "$location has operationId '${op.operationId}' but expected '${lowerCamelCase(op.summary)}' to match '${op.summary}'"
            else -> null
        }
    }

    private fun lowerCamelCase(text: String): String {
        return text.split(Regex("\\W+")).filter(String::isNotEmpty).mapIndexed { index, word ->
            when (index) {
                0 -> when (word) {
                    word.toUpperCase() -> word.toLowerCase()
                    else -> word.decapitalize()
                }
                else -> word.capitalize()
            }
        }.joinToString(separator = "")
    }
}
