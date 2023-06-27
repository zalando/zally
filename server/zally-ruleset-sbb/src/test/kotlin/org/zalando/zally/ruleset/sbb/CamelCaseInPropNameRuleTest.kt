package org.zalando.zally.ruleset.sbb

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.rulesConfig

class CamelCaseInPropNameRuleTest {

    private val rule = CamelCaseInPropNameRule(rulesConfig)

    @Test
    fun `checkPropertyNames should return violation if a property name is not camelCase`() {
        @Language("YAML")
        val spec = """
            openapi: '3.0.1'
            components:
              schemas:
                article:
                  properties:
                    super_mega_laser_turbo_article:
                      type: boolean
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkPropertyNames(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Property name has to be camelCase")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/article/properties/super_mega_laser_turbo_article")
    }

    @Test
    fun `checkPropertyNames should return no violation if only camelCase properties are used`() {
        @Language("YAML")
        val spec = """
            openapi: '3.0.1'
            components:
              schemas:
                article:
                  properties:
                    articleTitle:
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
