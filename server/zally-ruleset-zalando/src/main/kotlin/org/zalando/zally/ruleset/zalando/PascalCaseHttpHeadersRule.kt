package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.CaseChecker
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "132",
    severity = Severity.SHOULD,
    title = "Prefer Hyphenated-Pascal-Case for HTTP header fields"
)
class PascalCaseHttpHeadersRule(config: Config) {

    val description = "Header has to be Hyphenated-Pascal-Case"

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.SHOULD)
    fun checkHttpHeaders(context: Context): List<Violation> =
        checker.checkHeadersNames(context)
            .map { Violation(description, it.pointer) }
}
