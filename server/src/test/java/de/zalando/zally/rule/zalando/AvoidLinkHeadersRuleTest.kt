package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    private val rule = AvoidLinkHeadersRule(testConfig)

    @Test
    fun `a Swagger API with no header called Link produces no violation`() {
        @Language("YAML")
        val context = Context.createSwaggerContext("""
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
                        Location:
                          type: string
                          format: url
            parameters:
              FlowId:
                name: X-Flow-Id
                in: header
                type: string
                required: false
              Authorization:
                name: Authorization
                in: header
                type: string
                required: true
              ProductId:
                name: product_id
                in: path
                type: string
                required: true
        """.trimIndent(), failOnParseErrors = true)!!
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `an OpenAPI 3 API with no header called Link produces no violation`() {
        @Language("YAML")
        val context = Context.createOpenApiContext("""
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
        """.trimIndent(), failOnParseErrors = true)!!
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `an API with Link headers causes violations`() {
        @Language("YAML")
        val context = Context.createSwaggerContext("""
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
        """.trimIndent(), failOnParseErrors = true)!!
        val violations = rule.validate(context)
        assertThat(violations).hasSameElementsAs(listOf(
            v("/paths/~1foo/get/parameters/1"),
            v("/paths/~1foo/post/responses/202/headers/Link")
        ))
    }

    private fun v(pointer: String) = Violation(
        description = "Do Not Use Link Headers with JSON entities",
        pointer = JsonPointer.compile(pointer)
    )
}
