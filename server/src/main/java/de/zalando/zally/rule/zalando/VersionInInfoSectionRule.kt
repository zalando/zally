package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

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
            version == null -> context.violation("$description: version is missing", JsonPointer.compile("/info/version"))
            !version.matches(versionRegex) -> context.violation("$description: incorrect format", JsonPointer.compile("/info/version"))
            else -> null
        }
    }
}
