package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Contact
import io.swagger.models.Info
import io.swagger.models.Swagger

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "218",
    severity = Severity.MUST,
    title = "Contain API Meta Information"
)
class ApiMetainformationRule {

    private val versionRegex = """^\d+.\d+.\d+-?[\w+]*""".toRegex()
    private val basePath = "/info"
    private val noInfoBlock = "Info block should be provided"

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val info = swagger.info
        return when {
            info != null -> validateInfo(info)
            else -> Violation(noInfoBlock, listOf(basePath))
        }
    }

    private fun validateInfo(info: Info): Violation? {

        if (info.title.isNullOrBlank()) {
            return Violation("Title is not defined", listOf("$basePath/title"))
        }
        if (info.description.isNullOrBlank()) {
            return Violation("Description is not defined", listOf("$basePath/description"))
        }

        val versionValidation = checkVersion(info.version)
        if (versionValidation != null) {
            return versionValidation
        }
        return checkContact(info.contact)
    }

    private fun checkContact(contact: Contact?): Violation? =
        when {
            contact == null ->
                Violation("Contacts are not provided", listOf("$basePath/contact"))
            contact.name.isNullOrBlank() ->
                Violation("Contact name is empty", listOf("$basePath/contact/name"))
            contact.url.isNullOrBlank() ->
                Violation("Contact url is empty", listOf("$basePath/contact/url"))
            contact.email.isNullOrBlank() ->
                Violation("Contact email is empty", listOf("$basePath/contact/email"))
            else -> null
        }


    private fun checkVersion(version: String?): Violation? =
        when {
            version.isNullOrBlank() ->
                Violation("Version is not defined", listOf("$basePath/version"))
            !versionRegex.matches(version!!) ->
                Violation("Version is not following Semver rules", listOf("$basePath/version"))
            else -> null
        }


}