package de.zalando.zally.ruleset.sbb

import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "publication/#must-centralized-api-documentation-in-english",
    severity = Severity.MUST,
    title = "Contain API Meta Information"
)
class ApiMetaInformationRule {

    private val versionRegex = """^\d+.\d+(.\d+)?""".toRegex()

    @Check(severity = Severity.MUST)
    fun checkInfoTitle(context: Context): Violation? =
        if (context.api.info?.title.isNullOrBlank()) {
            context.violation("Title has to be provided", "/info/title".toJsonPointer())
        } else null

    @Check(severity = Severity.MUST)
    fun checkInfoDescription(context: Context): Violation? =
        if (context.api.info?.description.isNullOrBlank()) {
            context.violation("Description has to be provided", "/info/description".toJsonPointer())
        } else null

    @Check(severity = Severity.MUST)
    fun checkInfoVersion(context: Context): Violation? {
        val version = context.api.info?.version
        return when {
            version == null || version.isBlank() ->
                context.violation("Version has to be provided", "/info/version".toJsonPointer())
            !versionRegex.matches(version) ->
                context.violation("Version has to follow the Semver rules", "/info/version".toJsonPointer())
            else -> null
        }
    }

    @Check(severity = Severity.MUST)
    fun checkContactName(context: Context): Violation? =
        if (context.api.info?.contact?.name.isNullOrBlank()) {
            context.violation("Contact name has to be provided", "/info/contact/name".toJsonPointer())
        } else null

    @Check(severity = Severity.MUST)
    fun checkContactUrl(context: Context): Violation? =
        if (context.api.info?.contact?.url.isNullOrBlank()) {
            context.violation("Contact URL has to be provided", "/info/contact/url".toJsonPointer())
        } else null

    @Check(severity = Severity.MUST)
    fun checkContactEmail(context: Context): Violation? =
        if (context.api.info?.contact?.email.isNullOrBlank()) {
            context.violation("Contact e-mail has to be provided", "/info/contact/email".toJsonPointer())
        } else null
}
