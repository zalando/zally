package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.rule.api.Context
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.ruleset.zalando.model.ApiAudience

class FunctionalNamingForHostnamesRuleTest {

    private val rule = FunctionalNamingForHostnamesRule(rulesConfig)

    @Test
    fun `isUrlValid should return true if url follows the functional naming schema`() {
        assertThat(rule.isUrlValid("infrastructure-service.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isTrue()
        assertThat(rule.isUrlValid("infrastructure-api-linter.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com/", ApiAudience.EXTERNAL_PUBLIC)).isTrue()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.zalandoapis.com/api/", ApiAudience.EXTERNAL_PUBLIC)).isTrue()
        assertThat(rule.isUrlValid("https://some.api.zalan.do", ApiAudience.COMPONENT_INTERNAL)).isTrue()
    }

    @Test
    fun `isUrlValid should return false if url doesn't follow the functional naming schema`() {
        assertThat(rule.isUrlValid("", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("random text", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("maxim@some.host", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("infrastructure.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("special+characters.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("http://infrastructure-api-linter.zalandoapis.com", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("https://infrastructure-api-linter.othercompanysapis.com", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("https://some-api.zalan.do", ApiAudience.EXTERNAL_PUBLIC)).isFalse()
        assertThat(rule.isUrlValid("https://some-api.zalan.do", ApiAudience.EXTERNAL_PARTNER)).isFalse()
        assertThat(rule.isUrlValid("https://some-api.zalan.do", ApiAudience.BUSINESS_UNIT_INTERNAL)).isFalse()
    }

    @Test
    fun `mustFollowFunctionalNaming should return violation for external-public APIs with invalid hostname`() {
        val context = getOpenApiContextWithAudienceAndHostname("external-public", "incorrect url")

        val violations = rule.mustFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `mustFollowFunctionalNaming should return violation for an internal APIs with invalid hostname`() {
        val context = getOpenApiContextWithAudienceAndHostname("company-internal", "https://some.url")

        val violations = rule.mustFollowFunctionalNaming(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `shouldFollowFunctionalNaming should return violation for company-internal APIs with invalid hostname`() {
        val context = getOpenApiContextWithAudienceAndHostname("company-internal", "incorrect url")

        val violations = rule.shouldFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `mayFollowFunctionalNaming should return violation for component-internal APIs with invalid hostname`() {
        val context = getOpenApiContextWithAudienceAndHostname("component-internal", "incorrect url")

        val violations = rule.mayFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0/url")
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations if audience is not set`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
        """
        )

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations if audience is null`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience:
            servers:
              - url: "infrastructure-service.zalandoapis.com"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations if audience is invalid`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: invalid-audience
            servers:
              - url: "infrastructure-service.zalandoapis.com"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations for 'external-partner' audience and url from a exception list`() {
        val context = getOpenApiContextWithAudienceAndHostname("external-partner", "api-sandbox.merchants.zalando.com")

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    private fun getOpenApiContextWithAudienceAndHostname(audience: String, url: String): Context {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            info:
              x-audience: $audience
            servers:
              - url: $url
        """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }

    @Test
    fun `mustFollowFunctionalNaming should return violation for external-public Swagger APIs with invalid hostname`() {
        val context = getSwaggerContextWith(audience = "external-public", url = "incorrect url")

        val violations = rule.mustFollowFunctionalNaming(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*follow the functional naming schema.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/host")
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations for external-public Swagger APIs with valid hostname`() {
        val context = getSwaggerContextWith(audience = "external-public", url = "infrastructure-api-linter.zalandoapis.com")

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations for Swagger APIs with missing hostname`() {
        val context = getSwaggerContextWith(audience = "external-public", url = null)

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations for Swagger APIs with no audience`() {
        val context = getSwaggerContextWith(audience = null, url = "infrastructure-api-linter.zalandoapis.com")

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    @Test
    fun `(must, should, may)FollowFunctionalNaming should return no violations for OpenAPIs with hosts from the exception list for 'external-partner'`() {
        val context = getSwaggerContextWith(audience = "external-partner", url = "api.merchants.zalando.com")

        assertThat(rule.mustFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.shouldFollowFunctionalNaming(context)).isEmpty()
        assertThat(rule.mayFollowFunctionalNaming(context)).isEmpty()
    }

    private fun getSwaggerContextWith(audience: String?, url: String?): Context {

        val content = listOfNotNull(
            """
            swagger: '2.0'
            schemes:
              - https
            """,
            audience?.let {
                """
                info:
                    x-audience: $audience
                """
            },
            url?.let {
                """
                host: $url
                """
            }
        ).joinToString(separator = "\n", transform = String::trimIndent)

        return DefaultContextFactory().getSwaggerContext(content)
    }
}
