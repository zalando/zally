package org.zalando.zally.ruleset.zalando

import org.zalando.zally.test.ZallyAssertions.assertThat
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.core.DefaultContextFactory
import org.intellij.lang.annotations.Language
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    private val rule = AvoidLinkHeadersRule(rulesConfig)

    @Test
    fun `a Swagger API with no header called Link produces no violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            info:
              title: Clean Swagger API
            paths:
              /foo:
                get:
                  description: Lorem Ipsum
                  responses:
                    202:
                      description: Lorem Ipsum
                      headers:
                        Location: # should not violate since not called `Link`
                          type: string
                          format: url
            parameters:
              FlowId: # should not violate since not named `Link`
                name: X-Flow-Id
                in: header
                type: string
              Link: # should not violate since not a header
                name: Link
                in: query
                type: string
              ProductId: # should not violate since not a header nor named `Link`
                name: product_id
                in: path
                type: string
        """.trimIndent()
        )
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `an OpenAPI 3 API with no header called Link produces no violation`() {
        @Language("YAML")
        val context = DefaultContextFactory().getOpenApiContext(
            """
            openapi: 3.0.0
            info:
              title: Clean Swagger API
              version: 1.0.0
            paths:
              /foo:
                get:
                  description: Lorem Ipsum
                  responses:
                    202:
                      description: Lorem Ipsum
                      headers:
                        Location:
                          schema:
                            type: string
                            format: url
            components:
              parameters:
                FlowId:
                  name: X-Flow-Id
                  in: header
                  required: false
                  schema:
                    type: string
                Authorization:
                  name: Authorization
                  in: header
                  required: true
                  schema:
                    type: string
                ProductId:
                  name: product_id
                  in: path
                  required: true
                  schema:
                    type: string
        """.trimIndent()
        )
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `an API with Link headers causes violations`() {
        @Language("YAML")
        val context = DefaultContextFactory().getSwaggerContext(
            """
            swagger: 2.0
            info:
              title: Clean Swagger API
            paths:
              /foo:
                get:
                  parameters:
                    - name: Authorization
                      in: header
                      type: string
                    - name: Link
                      in: header
                      type: string
                  responses:
                    202:
                      description: Lorem Ipsum
                      headers:
                        Location:
                          type: string
                          format: url
                post:
                  responses:
                    202:
                      description: Lorem Ipsum
                      headers:
                        Link:
                          type: string
                          format: url
        """.trimIndent()
        )
        val violations = rule.validate(context)
        assertThat(violations)
            .descriptionsAllEqualTo("Do Not Use Link Headers with JSON entities")
            .pointersEqualTo(
                "/paths/~1foo/get/parameters/1",
                "/paths/~1foo/post/responses/202/headers/Link"
            )
    }
}
