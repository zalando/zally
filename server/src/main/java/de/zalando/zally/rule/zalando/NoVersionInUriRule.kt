package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "115",
        severity = Severity.MUST,
        title = "Do Not Use URI Versioning"
)
class NoVersionInUriRule {
    private val description = "basePath attribute contains version number"

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
/*        val hasVersion = adapter.openAPI.basePath != null && PatternUtil.hasVersionInUrl(adapter.openAPI.basePath)
        return if (hasVersion) Violation(description, emptyList()) else null*/
        // TODO implement it
        return null
    }
}
