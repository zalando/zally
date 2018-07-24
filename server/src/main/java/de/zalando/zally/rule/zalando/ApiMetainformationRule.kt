package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

/**
 * Set of violation checks for [Rule 218][http://zalando.github.io/restful-api-guidelines/#218]
 */
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "218",
    severity = Severity.MUST,
    title = "Contain API Meta Information"
)
class ApiMetainformationRule {

    private val versionRegex = """^\d+.\d+(.\d+)?""".toRegex()
    private val basePath = "/info"

    /**
     * Check info block
     */
    @Check(severity = Severity.MUST)
    fun validateInfoBlock(swagger: Swagger): Violation? {
        val info = swagger.info
        return when (info) {
            null -> Violation("Info block should be provided", listOf(basePath))
            else -> null
        }
    }

    /**
     * Check title
     */
    @Check(severity = Severity.MUST)
    fun validateInfoTitle(swagger: Swagger): Violation? =
        if (swagger.info?.title.isNullOrBlank()) {
            Violation("Title is not defined", listOf("$basePath/title"))
        } else null

    /**
     * Check description
     */
    @Check(severity = Severity.MUST)
    fun validateInfoDescription(swagger: Swagger): Violation? =
        if (swagger.info?.description.isNullOrBlank()) {
            Violation("Description is not defined", listOf("$basePath/description"))
        } else null

    /**
     * Check version property
     */
    @Check(severity = Severity.MUST)
    fun validateInfoVersion(swagger: Swagger): Violation? {
        val version = swagger.info?.version
        return when {
            version == null || version.isBlank() ->
                Violation("Version is not defined", listOf("$basePath/version"))
            !versionRegex.matches(version) ->
                Violation("Version is not following Semver rules", listOf("$basePath/version"))
            else -> null
        }
    }

    /**
     * Check contacts
     */
    @Check(severity = Severity.MUST)
    fun validateContact(swagger: Swagger): Violation? =
        when (swagger.info?.contact) {
            null -> Violation("Contacts are not provided", listOf("$basePath/contact"))
            else -> null
        }

    /**
     * Check contact name
     */
    @Check(severity = Severity.MUST)
    fun validateContactName(swagger: Swagger): Violation? =
        if (swagger.info?.contact?.name.isNullOrBlank()) {
            Violation("Contact name is empty", listOf("$basePath/contact/name"))
        } else null

    /**
     * Check contact URL
     */
    @Check(severity = Severity.MUST)
    fun validateContactUrl(swagger: Swagger): Violation? =
        if (swagger.info?.contact?.url.isNullOrBlank()) {
            Violation("Contact url is empty", listOf("$basePath/contact/url"))
        } else null

    /**
     * Check contact email
     */
    @Check(severity = Severity.MUST)
    fun validateContactEmail(swagger: Swagger): Violation? =
        if (swagger.info?.contact?.email.isNullOrBlank()) {
            Violation("Contact email is empty", listOf("$basePath/contact/email"))
        } else null
}