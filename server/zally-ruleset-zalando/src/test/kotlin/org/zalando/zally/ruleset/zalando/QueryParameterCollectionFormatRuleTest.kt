package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.DefaultContextFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class QueryParameterCollectionFormatRuleTest {

    private val rule = QueryParameterCollectionFormatRule()

    @Test
    fun `checkParametersCollectionFormat should return violation if invalid collection format is set`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            components:
              parameters:
                filters:
                  in: query
                  style: spaceDelimited
                  schema:
                    type: array
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkParametersCollectionFormat(context)

        assertThat(violations).isNotEmpty
        assertThat(violations.size).isEqualTo(1)
        assertThat(violations[0].description).isEqualTo("Parameter style have to be `form`")
        assertThat(violations[0].pointer.toString()).isEqualTo("/components/parameters/filters")
    }

    @Test
    fun `checkParametersCollectionFormat should return no violation if valid collection format is set`() {
        @Language("YAML")
        val content = """
            openapi: 3.0.1
            components:
              parameters:
                filters:
                  in: query
                  style: form
                  schema:
                    type: array
        """.trimIndent()
        val context = DefaultContextFactory().getOpenApiContext(content)

        val violations = rule.checkParametersCollectionFormat(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `checkParametersCollectionFormat should ignore OpenAPI 2 (Swagger) specifications`() {
        @Language("YAML")
        val content = """
            swagger: 2.0
            info:
              title: Old API
              version: 1.0.0
            parameters:
              filters:
                name: filters
                in: query
                type: array
                items:
                  type: string
        """.trimIndent()
        val context = DefaultContextFactory().getSwaggerContext(content)

        val violations = rule.checkParametersCollectionFormat(context)

        assertThat(violations).isEmpty()
    }
}
