package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.rulesConfig
import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.rule.api.Context
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class ProprietaryHeadersRuleTest {

    private val rule = ProprietaryHeadersRule(rulesConfig)

    @Test
    fun `validateRequestHeaders should return no violation for a standard header`() {
        val violations = rule.validateRequestHeaders(withRequestHttpHeader("From"))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `validateRequestHeaders should return no violation for a specified proprietary header`() {
        val violations = rule.validateRequestHeaders(withRequestHttpHeader("X-Flow-ID"))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `validateRequestHeaders should return a violation if a header is not specified`() {
        val violations = rule.validateRequestHeaders(withRequestHttpHeader("X-Some-Weird-Header"))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*use only standardized or specified request headers.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get/parameters/0")
    }

    @Test
    fun `validateResponseHeaders should return no violation for a standard header`() {
        val violations = rule.validateResponseHeaders(withResponseHttpHeader("Last-Modified"))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `validateResponseHeaders should return no violation for a specified proprietary header`() {
        val violations = rule.validateResponseHeaders(withRequestHttpHeader("X-Flow-ID"))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `validateResponseHeaders should return a violation if a header is not specified`() {
        val violations = rule.validateResponseHeaders(withResponseHttpHeader("X-Some-Weird-Header"))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*use only standardized or specified response headers.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1pets/get/responses/200/headers/X-Some-Weird-Header")
    }

    @Test
    fun `validateRequestHeaders should be not case-sensitive`() {
        val violations = rule.validateRequestHeaders(withRequestHttpHeader("hoSt"))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `validate(Request|Response)Headers should return no violation for empty API`() {
        @Language("YAML")
        val violations = rule.validateRequestHeaders(
            DefaultContextFactory().getOpenApiContext(
                """
            openapi: 3.0.1
        """
            )
        )

        assertThat(violations).isEmpty()
    }

    private fun withRequestHttpHeader(header: String): Context {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /pets:
                get:
                  parameters:
                    - name: $header
                      in: header
        """.trimIndent()
        return DefaultContextFactory().getOpenApiContext(content)
    }

    private fun withResponseHttpHeader(header: String): Context {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              '/pets':
                get:
                  responses:
                    200:
                      headers:
                        $header:
                          description: header in test
        """.trimIndent()
        return DefaultContextFactory().getOpenApiContext(content)
    }
}
