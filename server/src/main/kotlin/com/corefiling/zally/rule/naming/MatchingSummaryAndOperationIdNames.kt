package com.corefiling.zally.rule.naming

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Operation
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "MatchingSummaryAndOperationIdNames",
        severity = Severity.SHOULD,
        title = "Path Parameters Are Proceeded by Plurals"
)
class MatchingSummaryAndOperationIdNames : AbstractRule() {
    val description = "OperationId should be the lowerCamelCase version of the summary"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().map { (method, op) ->
                            validate("$pattern $method", op)
                        }.filterNotNull()
                    }
                    .ifNotEmptyLet { Violation(description, it) }

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
