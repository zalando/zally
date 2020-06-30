package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "116",
    severity = Severity.MUST,
    title = "Use Semantic Versioning"
)
class VersionInInfoSectionRule {
    private val description = "Semantic versioning has to be used in format MAJOR.MINOR(.DRAFT)"
    internal val versionRegex = """^\d+.\d+(.\d+)?$""".toRegex()

    @Check(severity = Severity.MUST)
    fun checkAPIVersion(context: Context): Violation? {
        val version = context.api.info?.version?.trim()
        return when {
            version == null -> context.violation("$description: version is missing", "/info/version".toJsonPointer())
            !version.matches(versionRegex) -> context.violation("$description: incorrect format", "/info/version".toJsonPointer())
            else -> null
        }
    }
}
