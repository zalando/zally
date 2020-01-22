package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.rulesConfig
import de.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SnakeCaseForQueryParamsRuleTest {

    private val rule = SnakeCaseForQueryParamsRule(rulesConfig)

    @Test
    fun `checkQueryParameter should return violation if query parameter is not snake_case`() {
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  parameters:
                    - name: filterExpensiveArticles
                      in: query
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkQueryParameter(context)

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Query parameter has to be snake_case")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1article/get/parameters/0")
    }

    @Test
    fun `checkQueryParameter should return no violation if query parameters are snake_case`() {
        val spec = """
            openapi: 3.0.1
            paths:
              /article:
                get:
                  parameters:
                    - name: filter_expensive_articles
                      in: query
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(spec)

        val violations = rule.checkQueryParameter(context)

        assertThat(violations).isEmpty()
    }
}
