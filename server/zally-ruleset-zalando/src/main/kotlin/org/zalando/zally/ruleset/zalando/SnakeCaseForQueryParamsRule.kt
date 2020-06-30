package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.CaseChecker
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

/**
 * Lint for snake case for query params
 */
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "130",
    severity = Severity.MUST,
    title = "Use snake_case (never camelCase) for Query Parameters"
)
class SnakeCaseForQueryParamsRule(config: Config) {

    val description = "Query parameter has to be snake_case"

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.MUST)
    fun checkQueryParameter(context: Context): List<Violation> =
        checker.checkQueryParameterNames(context).map { Violation(description, it.pointer) }
}
