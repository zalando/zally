package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.Context
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class FunctionalNamingForHostnamesRuleTest {

    private val rule = FunctionalNamingForHostnamesRule()

    @Test
    fun `isUrlValid should return true if url follows the functional naming schema`() {
        assertThat(rule.isUrlValid("infrastructure-service.zalandoapis.com")).isTrue()
        assertThat(rule.isUrlValid("infrastructure-api-linter.zalandoapis.com")).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com")).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com/")).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com/api/")).isTrue()
    }

    @Test
    fun `isUrlValid should return false if url doesn't follow the functional naming schema`() {
        assertThat(rule.isUrlValid("")).isFalse()
        assertThat(rule.isUrlValid("random text")).isFalse()
        assertThat(rule.isUrlValid("maxim@some.host")).isFalse()
        assertThat(rule.isUrlValid("infrastructure.zalandoapis.com")).isFalse()
        assertThat(rule.isUrlValid("special+characters.zalandoapis.com")).isFalse()
        assertThat(rule.isUrlValid("http://infrastructure-api-linter.zalandoapis.com")).isFalse()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.othercompanysapis.com")).isFalse()
    }

    @Test
    fun `mustFollowFunctionalNaming should return violation for external-public APIs with invalid hostname`() {
        val context = withAudienceAndHostname("external-public", "incorrect url")

        val violations = rule.mustFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `shouldFollowFunctionalNaming should return violation for company-internal APIs with invalid hostname`() {
        val context = withAudienceAndHostname("company-internal", "incorrect url")

        val violations = rule.shouldFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `mayFollowFunctionalNaming should return violation for component-internal APIs with invalid hostname`() {
        val context = withAudienceAndHostname("component-internal", "incorrect url")

        val violations = rule.mayFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `(must|should|may)FollowFunctionalNaming should return no violations if audience is not set`() {
        val context = Context.createOpenApiContext("openapi: 3.0.1")!!

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    private fun withAudienceAndHostname(audience: String, url: String): Context {
        val content = """
            openapi: 3.0.1
            info:
              x-audience: $audience
            servers:
              - url: $url
            """.trimIndent()

        return Context.createOpenApiContext(content)!!
    }
}
