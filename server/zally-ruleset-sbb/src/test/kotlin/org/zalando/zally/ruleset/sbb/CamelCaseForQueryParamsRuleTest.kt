package org.zalando.zally.ruleset.sbb

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.rulesConfig

class CamelCaseForQueryParamsRuleTest {

    private val rule = CamelCaseForQueryParamsRule(rulesConfig)

    @Test
    fun `checkQueryParameter should return violation if query parameter is not camelCase`() {
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

        assertThat(violations).isNotEmpty
        assertThat(violations[0].description).isEqualTo("Query parameter has to be camelCase")
        assertThat(violations[0].pointer.toString()).isEqualTo("/paths/~1article/get/parameters/0")
    }

    @Test
    fun `checkQueryParameter should return no violation if query parameters are camelCase`() {
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

        assertThat(violations).isEmpty()
    }
}
