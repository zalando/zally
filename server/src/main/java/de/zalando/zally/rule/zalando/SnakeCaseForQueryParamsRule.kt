package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllParameters

/**
 * Lint for snake case for query params
 */
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "130",
    severity = Severity.MUST,
    title = "Use snake_case (never camelCase) for Query Parameters"
)
class SnakeCaseForQueryParamsRule {

    val description = "Query parameter has to be snake_case"

    @Check(severity = Severity.MUST)
    fun checkQueryParameter(context: Context): List<Violation> =
        context.api.getAllParameters().values
            .filter { "query" == it.`in` }
            .filterNot { PatternUtil.isSnakeCase(it.name) }
            .map { context.violation(description, it) }
}
