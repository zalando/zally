package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "132",
    severity = Severity.SHOULD,
    title = "Prefer Hyphenated-Pascal-Case for HTTP header fields"
)
class PascalCaseHttpHeadersRule(config: Config) {

    val description = "Query parameter has to be snake_case"

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.SHOULD)
    fun checkHttpHeaders(context: Context): List<Violation> =
        checker.checkHeadersNames(context)
            .map { Violation("Header has to be Hyphenated-Pascal-Case", it.pointer) }
}
