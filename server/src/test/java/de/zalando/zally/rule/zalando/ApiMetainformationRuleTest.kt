package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApiMetainformationRuleTest {

    val rule = ApiMetainformationRule()
    val swagger = getFixture("218.yaml")

    @Test
    fun validationPassed() =
        assertThat(validate(swagger).filterNotNull()).isEmpty()


    @Test
    fun infoBlockNotFound() {
        val expected = Violation("Info block should be provided", listOf("/info"))
        val emptyDefinition = Swagger()
        val violation = rule.validateInfoBlock(emptyDefinition)
        assertThat(violation).isEqualTo(expected)
    }

    @Test
    fun checkInvalidTitle() {
        val expected = Violation("Title is not defined", listOf("/info/title"))
        swagger.info?.title = ""
        val violation = rule.validateInfoTitle(swagger)
        assertThat(violation).isEqualTo(expected)
    }

    @Test
    fun checkInvalidDescription() {
        val expected = Violation("Description is not defined", listOf("/info/description"))
        swagger.info?.description = ""
        val violation = rule.validateInfoDescription(swagger)
        assertThat(violation).isEqualTo(expected)
    }

    @Test
    fun checkVersionNotDefined() {
        val expected = Violation("Version is not defined", listOf("/info/version"))
        swagger.info?.version = ""
        val violation = rule.validateInfoVersion(swagger)
        assertThat(violation).isEqualTo(expected)
    }

    @Test
    fun versionHasWrongFormat() {
        var violation = rule.validateInfoVersion(swagger)
        assertThat(violation).isNull()

        val expected = Violation("Version is not following Semver rules", listOf("/info/version"))
        swagger.info.version = "abc.32.32"
        violation = rule.validateInfoVersion(swagger)
        assertThat(violation).isEqualTo(expected)

        swagger.info.version = "1.d.1"
        violation = rule.validateInfoVersion(swagger)
        assertThat(violation).isEqualTo(expected)

        swagger.info.version = "some version"
        violation = rule.validateInfoVersion(swagger)
        assertThat(violation).isEqualTo(expected)
    }

    @Test
    fun invalidContact() {
        val contact = swagger.info.contact
        swagger.info.contact = null
        var violation = rule.validateContact(swagger)
        assertThat(violation).isEqualTo(Violation("Contacts are not provided", listOf("/info/contact")))

        swagger.info.contact = contact

        val oldName = contact.name
        contact.name = ""
        violation = rule.validateContactName(swagger)
        assertThat(violation).isEqualTo(Violation("Contact name is empty", listOf("/info/contact/name")))
        contact.name = oldName

        val oldUrl = contact.url
        contact.url = ""
        violation = rule.validateContactUrl(swagger)
        assertThat(violation).isEqualTo(Violation("Contact url is empty", listOf("/info/contact/url")))
        contact.url = oldUrl

        contact.email = ""
        violation = rule.validateContactEmail(swagger)
        assertThat(violation).isEqualTo(Violation("Contact email is empty", listOf("/info/contact/email")))
    }

    private fun validate(swagger: Swagger): List<Violation?> =
        listOf(rule.validateInfoBlock(swagger),
            rule.validateInfoTitle(swagger),
            rule.validateInfoDescription(swagger),
            rule.validateInfoVersion(swagger),
            rule.validateContact(swagger),
            rule.validateContactName(swagger),
            rule.validateContactUrl(swagger),
            rule.validateContactEmail(swagger))


}