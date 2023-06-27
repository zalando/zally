package org.zalando.zally.ruleset.sbb

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory

class VersionInUriRuleTest {

    private val rule = VersionInUriRule()

    @Test
    fun `checkServerURLs should return a violation if host names contain version`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.v1.net/api/"
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("Version found in host Name")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkResourceNames should return a violation if (sub) resource names contain version`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /shop/orders-v1: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkResourceNames(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("Version found in resource name")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1shop~1orders-v1")
    }

    @Test
    fun `checkResourceNames should return no violation if resource name is a plain version only`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /api/v1/shop/{order-id}: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkResourceNames(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkPathUrls should return a violation if url contains no version`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /shop/orders/{order-id}: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPathUrls(context)

        assertThat(violations).isNotEmpty
        assertThat(violations).hasSize(1)
        assertThat(violations[0].description).contains("No Version found in Path")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1shop~1orders~1{order-id}")
    }

    @Test
    fun `checkPathUrls should return no violations if a path does contain a version`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /v1/shop/orders/{order-id}: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPathUrls(context)

        assertThat(violations).isEmpty()
    }
}
