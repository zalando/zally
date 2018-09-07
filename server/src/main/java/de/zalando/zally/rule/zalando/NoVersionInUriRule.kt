package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "115",
    severity = Severity.MUST,
    title = "Do Not Use URI Versioning"
)
class NoVersionInUriRule {
    private val description = "Server URL contains version number"

    @Check(severity = Severity.MUST)
    fun checkServerURLs(context: Context): List<Violation> =
        context.api.servers.orEmpty()
            .filter { PatternUtil.hasVersionInUrl(it.url) }
            .map { context.violation(description, it) }
}
