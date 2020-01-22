package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensibleEnumRuleTest {

    val rule = ExtensibleEnumRule()

    @Test
    fun `checkForEnums should return violation if an enum is used in schema`() {
        val content = """
            openapi: 3.0.1
            components:
              schemas:
                article:
                  properties:
                    color:
                      type: string
                      enum:
                        - white
                        - black
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkForEnums(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern("Property is not an extensible enum.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/schemas/article/properties/color")
    }

    @Test
    fun `checkForEnums should return violation if an enum is used as parameter`() {
        val content = """
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
                          - germany
                          - sweden
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkForEnums(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern("Property is not an extensible enum.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1article/get/parameters/0/schema")
    }

    @Test
    fun `checkForEnums should return violation if no enums are used`() {
        val content = """
            openapi: 3.0.1
            components:
              schemas:
                article:
                  properties:
                    color:
                      type: string
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkForEnums(context)

        assertThat(violations).isEmpty()
    }
}
