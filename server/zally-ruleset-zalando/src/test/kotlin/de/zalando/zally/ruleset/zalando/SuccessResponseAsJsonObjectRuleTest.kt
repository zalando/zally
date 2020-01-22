package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class SuccessResponseAsJsonObjectRuleTest {

    private val rule = SuccessResponseAsJsonObjectRule()

    @Test
    fun `checkJSONObjectIsUsedAsSuccessResponseType should return violation if other then JSON object is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  responses:
                    200:
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              type: string
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkJSONObjectIsUsedAsSuccessResponseType(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Always return JSON objects as top-level data structures to support extensibility")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1article/get/responses/200/content/application~1json/schema")
    }

    @Test
    fun `checkJSONObjectIsUsedAsSuccessResponseType should return no violation if only JSON object is specified`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  responses:
                    200:
                      content:
                        application/json:
                          schema:
                            type: object
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkJSONObjectIsUsedAsSuccessResponseType(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkJSONObjectIsUsedAsSuccessResponseType should support referenced schema`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  responses:
                    200:
                      content:
                        application/json:
                          schema:
                            ${'$'}ref: '#/components/schemas/article'
            components:
              schemas:
                article:
                  properties:
                    name:
                      type: string
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkJSONObjectIsUsedAsSuccessResponseType(context)

        assertThat(violations).isEmpty()
    }
}
