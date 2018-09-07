package de.zalando.zally.rule.zalando

import de.zalando.zally.getOpenApiContextFromContent
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
        val context = getOpenApiContextFromContent(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).contains("Server URL contains version number")
        assertThat(violations[0].pointer.toString()).isEqualTo("/servers/0")
    }

    @Test
    fun `checkServerURLs should return no violations if a server URL does not contain a version as base path`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            servers:
              - url: "https://inter.net/api/"
        """.trimIndent()
        val context = getOpenApiContextFromContent(spec)

        val violations = rule.checkServerURLs(context)

        assertThat(violations).isEmpty()
    }
}
