package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.rulesConfig

class FormatForNumbersRuleTest {

    private val rule = FormatForNumbersRule(rulesConfig)

    @Test
    fun `should not return a violation if number format is set`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Awesome API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    age:
                      type: number
                      format: decimal
            """.trimIndent()

        val violations = rule.checkNumberFormat(DefaultContextFactory().getOpenApiContext(content))

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should return a violation if number format is not set`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Bad API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    age:
                      type: number
            """.trimIndent()

        val violations = rule.checkNumberFormat(DefaultContextFactory().getOpenApiContext(content))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).matches(".*Numeric properties must have valid format.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/Pet/properties/age")
    }

    @Test
    fun `should return a violation if invalid number format is set`() {
        @Language("YAML")
        val content = """
            openapi: '3.0.1'
            info:
              title: Weird API
              version: 1.0.0
            components:
              schemas:
                Pet:
                  properties:
                    age:
                      type: number
                      format: weird_number_format
            """.trimIndent()

        val violations = rule.checkNumberFormat(DefaultContextFactory().getOpenApiContext(content))

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).matches(".*Numeric properties must have valid format.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/Pet/properties/age")
    }

    @Test
    fun `should not explode when no schemas are present`() {
        @Language("YAML")
        val yaml = """
            swagger: '2.0'
            info:
              title: Empty API
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkNumberFormat(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should not explode when schemas is null`() {
        @Language("YAML")
        val yaml = """
            swagger: '2.0'
            info:
              title: Minimal API
              version: 1.0.0
            paths:
              /handlers:
                get:
                  responses:
                    200:
                      description: OK
                      examples:
                        application/json:
                          - name: Named thing
            """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(yaml)

        val violations = rule.checkNumberFormat(context)

        assertThat(violations).isEmpty()
    }
}
