package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class NoVersionInUriRuleTest {

    private val rule = NoVersionInUriRule()

    @Test
    fun `checkServerURLs should return a violation if a server URL contains a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.net/api/v1.0"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL contains version number")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return a violation if (sub) resource names contain version suffix`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /shop/orders-v1/{order-id}: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("URL contains version number")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1shop~1orders-v1~1{order-id}")
    }

    @Test
    fun `checkServerURLs should return no violations if a server URL does not contain a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.net/api/"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isEmpty()
    }
}
