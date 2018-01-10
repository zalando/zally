package com.corefiling.pdds.zally.rule.naming

import com.corefiling.pdds.zally.extensions.validateOperation
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
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
            swagger.validateOperation(description) { _, _, _, operation ->
                when {
                    operation.operationId == null -> "has no operationId!"
                    operation.summary == null -> "has no summary!"
                    operation.operationId != lowerCamelCase(operation.summary) -> "has operationId '${operation.operationId}' but expected '${lowerCamelCase(operation.summary)}' to match '${operation.summary}'"
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
