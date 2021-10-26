package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.Test
import org.zalando.zally.core.DefaultContextFactory

class EnumValueTypeTest {

    private val rule = EnumValueTypeRule()

    @Test
    fun `fail validation if 'x-extensible-enum' has a 'string' type`() {
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
                                type: integer
                                x-extensible-enum:
                                  - 1
                                  - 2
                                  - 3
                    
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        Assertions.assertThat(violations).hasSize(1)
    }

    @Test
    fun `pass validation if 'x-extensible-enum' has a 'string' type`() {
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
                                  - one
                                  - two
                                  - three
                    
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.validate(context)
        Assertions.assertThat(violations).isEmpty()
    }
}
