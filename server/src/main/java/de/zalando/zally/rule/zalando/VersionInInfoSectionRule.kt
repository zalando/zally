package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.PatternUtil.isVersion
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "116",
        severity = Severity.SHOULD,
        title = "Provide version information"
)
class VersionInInfoSectionRule : AbstractRule() {
    private val description = "Only the documentation, not the API itself, needs version information. It should be in the " +
        "format MAJOR.MINOR.DRAFT."

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val version = swagger.info?.version
        val desc = when {
            version == null -> "Version is missing"
            !isVersion(version) -> "Specified version has incorrect format: $version"
            else -> null
        }
        return desc?.let { Violation("$description $it", emptyList()) }
    }
}
