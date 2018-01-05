package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "115",
        severity = Severity.MUST,
        title = "Do Not Use URI Versioning"
)
class NoVersionInUriRule : AbstractRule() {
    private val description = "basePath attribute contains version number"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val hasVersion = swagger.basePath != null && PatternUtil.hasVersionInUrl(swagger.basePath)
        return if (hasVersion) Violation(description, emptyList()) else null
    }
}
