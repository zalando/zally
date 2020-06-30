package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class SnakeCaseInPropNameRuleTest {

    private val rule = SnakeCaseInPropNameRule(rulesConfig)

    @Test
    fun `checkPropertyNames should return violation if a property name is not snake_case`() {
        @Language("YAML")
        val spec = """
            openapi: '3.0.1'
            components:
              schemas:
                article:
                  properties:
                    superMegaLaserTurboArticle:
                      type: boolean
            """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPropertyNames(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Property name has to be snake_case")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/article/properties/superMegaLaserTurboArticle")
    }

    @Test
    fun `checkPropertyNames should return no violation if only snake_case properties are used`() {
        @Language("YAML")
        val spec = """
            openapi: '3.0.1'
            components:
              schemas:
                article:
                  properties:
                    article_title:
                      type: string
            """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPropertyNames(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkPropertyNames should return no violation if only whitelisted properties are used`() {
        @Language("YAML")
        val spec = """
            openapi: '3.0.1'
            components:
              schemas:
                article:
                  properties:
                    _links:
                      type: string
            """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPropertyNames(context)

        assertThat(violations).isEmpty()
    }
}
