package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isVersion

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "116",
        severity = Severity.SHOULD,
        title = "Provide version information"
)
class VersionInInfoSectionRule {
    private val description = "Only the documentation, not the API itself, needs version information. It should be in the " +
        "format MAJOR.MINOR.DRAFT."

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {
        val version = adapter.openAPI.info?.version
        val desc = when {
            version == null -> "Version is missing"
            !isVersion(version) -> "Specified version has incorrect format: $version"
            else -> null
        }
        return desc?.let { Violation("$description $it", emptyList()) }
    }
}
