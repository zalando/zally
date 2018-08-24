package de.zalando.zally.rule.zalando

import de.zalando.zally.getOpenApiContextFromContent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Use429HeaderForRateLimitRuleTest {

    private val rule = Use429HeaderForRateLimitRule()

    @Test
    fun `checkHeadersForRateLimiting should return violation if no rate limit is provided via headers`() {
        val content = """
            openapi: 3.0.1
            paths:
              /articles:
                get:
                  responses:
                    429: {}
        """.trimIndent()
        val context = getOpenApiContextFromContent(content)

        val violations = rule.checkHeadersForRateLimiting(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).containsPattern(".*has to contain rate limit information via headers.*")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1articles/get/responses/429")
    }

    @Test
    fun `checkHeadersForRateLimiting should return no violation if rate limit information is provided via headers`() {
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
        val context = getOpenApiContextFromContent(content)

        val violations = rule.checkHeadersForRateLimiting(context)

        assertThat(violations).isEmpty()
    }
}
