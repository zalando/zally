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
    id = "129",
    severity = Severity.MUST,
    title = "Lowercase words with hyphens"
)
class KebabCaseInPathSegmentsRule(config: Config) {

    private val checker = CaseChecker.load(config)

    private val description = "Use lowercase separate words with hyphens for path segments"
    internal val lowerCaseHyphenSeparatedRegex = "^[a-z-]+$".toRegex()

    @Check(severity = Severity.MUST)
    fun checkKebabCaseInPathSegments(context: Context): List<Violation> =
        checker.checkPathSegments(context).map { Violation(description, it.pointer) }
}
