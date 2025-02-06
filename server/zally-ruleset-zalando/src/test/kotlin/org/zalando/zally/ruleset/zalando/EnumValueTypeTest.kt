package org.zalando.zally.ruleset.zalando

import org.assertj.core.api.Assertions
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.zalando.zally.core.DefaultContextFactory
import java.util.stream.Stream

class EnumValueTypeTest {

    private val rule = EnumValueTypeRule()

    companion object {
        @JvmStatic
        fun nonStringTypes(): Stream<Arguments> = Stream.of(
            Arguments.of("number", "1", "2"),
            Arguments.of("integer", "1", "2"),
            Arguments.of("boolean", "true", "false")
        )
    }

    @ParameterizedTest
    @MethodSource("nonStringTypes")
    fun `fail validation if 'x-extensible-enum' has a non-'string' type`(nonStringType: String, value1: String, value2: String) {
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
                                type: $nonStringType
                                x-extensible-enum:
                                  - $value1
                                  - $value2
                    
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
