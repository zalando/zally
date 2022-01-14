package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class JsonProblemAsDefaultResponseRuleTest {

    val rule = JsonProblemAsDefaultResponseRule()

    @Test
    fun `checkContainsDefaultResponse should return violation if default response is not set`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
        """.trimIndent()
        )

        val violations = rule.checkContainsDefaultResponse(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*should contain the default response.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1resources/get")
    }

    @Test
    fun `checkContainsDefaultResponse should not return violation for empty specification`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
        """
        )

        assertThat(rule.checkContainsDefaultResponse(context)).isEmpty()
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonMediaType should not return violation for application-problem+json`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
                    default:
                      content:
                        application/problem+json:
                          schema:
                            ${'$'}ref: 'http://example.com'
        """
        )

        assertThat(rule.checkDefaultResponseIsProblemJsonMediaType(context)).isEmpty()
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonMediaType should return violation if not application-problem+json is set as default response`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
                    default:
                      content:
                        application/gzip:
                          schema:
                            ${'$'}ref: 'http://example.com'
        """.trimIndent()
        )

        val violations = rule.checkDefaultResponseIsProblemJsonMediaType(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("media-type application/problem+json should be used as default response")
        assertThat(violations[0].pointer.toString())
            .isEqualTo("/paths/~1resources/get/responses/default/content/application~1gzip")
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonMediaType should not return violation for empty specification`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
        """
        )

        assertThat(rule.checkDefaultResponseIsProblemJsonMediaType(context)).isEmpty()
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonSchema should return violation if incorrect problem+json schema reference is set as default response`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
                    default:
                      content:
                        application/problem+json:
                          schema:
                            ${'$'}ref: 'http://example.com'
        """.trimIndent()
        )

        val violations = rule.checkDefaultResponseIsProblemJsonSchema(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("problem+json should be used as default response")
        assertThat(violations[0].pointer.toString())
            .isEqualTo("/paths/~1resources/get/responses/default/content/application~1problem+json")
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonSchema should return no violation if last problem+json schema reference is set as default response`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
                    default:
                      content:
                        application/problem+json:
                          schema:
                            ${'$'}ref: 'https://zalando.github.io/problem/schema.yaml#/Problem'
        """.trimIndent()
        )

        val violations = rule.checkDefaultResponseIsProblemJsonSchema(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonSchema should return no violation if first problem+json schema reference is set as default response`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
            paths:
              '/resources':
                get:
                  responses:
                    default:
                      content:
                        application/problem+json:
                          schema:
                            ${'$'}ref: 'https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.1.yaml#/Problem'
        """.trimIndent()
        )

        val violations = rule.checkDefaultResponseIsProblemJsonSchema(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkDefaultResponseIsProblemJsonSchema should not return violation for empty specification`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.1
        """
        )

        assertThat(rule.checkDefaultResponseIsProblemJsonSchema(context)).isEmpty()
    }
}
