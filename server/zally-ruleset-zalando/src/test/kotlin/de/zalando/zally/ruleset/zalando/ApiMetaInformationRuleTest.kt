package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApiMetaInformationRuleTest {

    val rule = ApiMetaInformationRule()

    @Test
    fun `checkInfoTitle should return violation if title is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoTitle(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Title has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/title")
    }

    @Test
    fun `checkInfoTitle should return no violation if title is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              title: Awesome API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoTitle(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkInfoDescription should return violation if description is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoDescription(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Description has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/description")
    }

    @Test
    fun `checkInfoDescription should return no violation if description is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              description: super awesome mega turbo laser API
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoDescription(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkInfoVersion should return violation if version is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoVersion(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Version has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/version")
    }

    @Test
    fun `checkInfoVersion should return violation if version doesn't follow the Semver rules`() {
        val spec = """
            openapi: 3.0.1
            info:
              version: alpha-beta-gamma-version.1.1.1.1.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoVersion(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Version has to follow the Semver rules")
        assertThat(violation.pointer.toString()).isEqualTo("/info/version")
    }

    @Test
    fun `checkInfoVersion should return no violation if version is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              version: 1.0.0
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkInfoVersion(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkContactName should return violation if contact name is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactName(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Contact name has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/contact/name")
    }

    @Test
    fun `checkContactName should return no violation if contact name is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              contact:
                name: Awesome Team
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactName(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkContactUrl should return violation if contact URL is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactUrl(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Contact URL has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/contact/url")
    }

    @Test
    fun `checkContactUrl should return no violation if contact URL is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              contact:
                url: https://awesome-team.company.com
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactUrl(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `checkContactEmail should return violation if contact e-mail is not set`() {
        val spec = """
            openapi: 3.0.1
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactEmail(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).isEqualTo("Contact e-mail has to be provided")
        assertThat(violation.pointer.toString()).isEqualTo("/info/contact/email")
    }

    @Test
    fun `checkContactEmail should return no violation if contact e-mail is set`() {
        val spec = """
            openapi: 3.0.1
            info:
              contact:
                email: awesome-team@company.com
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violation = rule.checkContactEmail(context)

        assertThat(violation).isNull()
    }
}
