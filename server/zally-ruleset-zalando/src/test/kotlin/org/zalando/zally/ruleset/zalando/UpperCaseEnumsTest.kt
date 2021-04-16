package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.zalando.zally.core.DefaultContextFactory

class UpperCaseEnumsTest {

    private val rule = UpperCaseEnums()

    @Test
    fun `should return violation if x-extensible-enum format in parameter is incorrect`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  parameters:
                    - name: country
                      in: query
                      schema:
                        type: string
                        x-extensible-enum:
                          - GERMANY
                          - sweden
                          - spain
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        assertThat(violations).hasSize(2)
    }

    @Test
    fun `should return violation if x-extensible-enum format in schema property is invalid`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  responses: 
                    200:
                      description: The identifiers associated with the source id.
                      content: 
                        application/json:
                          schema:
                            type: object
                            properties:
                              prop-1:
                                type: string
                                x-extensible-enum:
                                  - GERMANY
                                  - sweden
                                  - spain
                    
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        assertThat(violations).hasSize(2)
    }

    @Test
    fun `rule should return violation if enum format is incorrect`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  parameters:
                    - name: country
                      in: query
                      schema:
                        type: string
                        enum:
                          - GERMANY
                          - sweden
                          - spain
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        assertThat(violations).hasSize(2)
    }

    @Test
    fun `rule should return no violations`() {
        @Language("YAML")
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  parameters:
                    - name: country
                      in: query
                      schema:
                        type: string
                        enum:
                          - GERMANY
                          - SWEDEN
                          - PUT_YOUR_COUNTRY_HERE
                          - COUNTRY_1
                  responses: 
                    200:
                      description: The identifiers associated with the source id.
                      content: 
                        application/json:
                          schema:
                            type: object
                            properties:
                              prop-1:
                                type: string
                                x-extensible-enum:
                                  - GERMANY
                                  - SWEDEN
                                  - SPAIN
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }
}
