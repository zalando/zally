package de.zalando.zally.ruleset.zally

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.test.ZallyAssertions
import org.intellij.lang.annotations.Language
import org.junit.Test

class NoProtocolInHostRuleTest {

    private val rule = NoProtocolInHostRule()

    @Test
    fun `validate swagger with empty swagger returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate swagger with simple hostname returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            host: google.com
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }

    @Test
    fun `validate swagger with http protocol included returns a violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            host: http://google.com
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .descriptionsEqualTo("'http://google.com' contains protocol information which should be listed separately as schemes")
            .pointersEqualTo("/host")
    }

    @Test
    fun `validate swagger with https protocol included returns a violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            host: https://google.com
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .descriptionsEqualTo("'https://google.com' contains protocol information which should be listed separately as schemes")
            .pointersEqualTo("/host")
    }

    @Test
    fun `validate openapi with url including protocol returns no violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            servers:
              - url: https://google.com
                description: OpenAPI expects a URL, not a hostname, so this is correct!
            """.trimIndent()
        )

        ZallyAssertions
            .assertThat(rule.validate(context))
            .isEmpty()
    }
}
