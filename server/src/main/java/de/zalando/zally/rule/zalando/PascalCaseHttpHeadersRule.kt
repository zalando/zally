package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllHeaders

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "132",
    severity = Severity.SHOULD,
    title = "Prefer Hyphenated-Pascal-Case for HTTP header fields"
)
class PascalCaseHttpHeadersRule {

    @Check(severity = Severity.SHOULD)
    fun checkHttpHeaders(context: Context): List<Violation> =
        context.api.getAllHeaders()
            .filterNot { PatternUtil.isHyphenatedPascalCase(it.name) }
            .map { context.violation("Header has to be Hyphenated-Pascal-Case", it.element) }
}
