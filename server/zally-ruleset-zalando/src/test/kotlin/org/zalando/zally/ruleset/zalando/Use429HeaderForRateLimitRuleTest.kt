package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class Use429HeaderForRateLimitRuleTest {

    private val rule = Use429HeaderForRateLimitRule()

    @Test
    fun `checkHeadersForRateLimiting should return violation if no rate limit is provided via headers`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /articles:
                get:
                  responses:
                    429: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHeadersForRateLimiting(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*has to contain rate limit information via headers.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1articles/get/responses/429")
    }

    @Test
    fun `checkHeadersForRateLimiting should return no violation if rate limit information is provided via headers`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            paths:
              /articles:
                get:
                  responses:
                    429:
                      headers:
                        Retry-After: {}
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkHeadersForRateLimiting(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkHeadersForRateLimiting avoid bug 787 NPE on missing response`() {
        @Language("YAML")
        val content = """
            swagger: "2.0"
            paths:
              /articles:
                get:
                  description: asd
            """.trimIndent()
        val context = DefaultContextFactory().getSwaggerContext(content)

        val violations = rule.checkHeadersForRateLimiting(context)

        assertThat(violations).isEmpty()
    }
}
