package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.DefaultContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UseProblemJsonRuleTest {

    private val rule = UseProblemJsonRule()

    @Test
    fun `should return violation if wrong media type is used as the default response`() {
        val content = """
        openapi: 3.0.1
        info:
          version: 1.0.0
          title: Test
        paths:
          /pets:
            get:
              responses:
                default:
                  content:
                    application/json:
                      schema:
                        ${'$'}ref: 'https://opensource.zalando.com/problem/schema.yaml#/Problem'
        """.trimIndent()

        val context = DefaultContext.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations.size).isEqualTo(1)
        assertThat(violations[0].pointer.toString())
            .isEqualTo("/paths/~1pets/get/responses/default/content/application~1json")
    }

    @Test
    fun `should return violation if wrong media type is used as the default response (OpenAPI 2)`() {
        val content = """
        swagger: '2.0'
        info:
          version: 1.0.0
          title: Pets API
        paths:
          "/pets":
            get:
              produces:
                - application/json
              responses:
                default:
                  description: Error object
                  schema:
                    ${'$'}ref: https://opensource.zalando.com/problem/schema.yaml#/Problem
        """.trimIndent()

        val context = DefaultContext.createSwaggerContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations.size).isEqualTo(1)
        assertThat(violations[0].pointer.toString())
            .isEqualTo("/paths/~1pets/get/responses/default")
    }

    @Test
    fun `should return violation if a wrong schema is used for Problem Details Object`() {
        val content = """
        openapi: 3.0.1
        info:
          version: 1.0.0
          title: Pets API
        paths:
          /bad:
            get:
              responses:
                default:
                  content:
                    application/problem+json:
                      schema:
                        type: object
                        properties:
                          status:
                            type: string
        """.trimIndent()

        val context = DefaultContext.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations.map { it.pointer.toString() }).containsExactlyInAnyOrder(
            "/paths/~1bad/get/responses/default/content/application~1problem+json/schema/properties/status",
            "/paths/~1bad/get/responses/default/content/application~1problem+json/schema/properties/status/type"
        )
    }

    @Test
    fun `should return violation if a incorrect schema is used for Problem Details Object (OpenAPI 2)`() {
        val content = """
        swagger: '2.0'
        info:
          version: 1.0.0
          title: Pets API
        paths:
          /pets:
            get:
              produces:
                - application/problem+json
              responses:
                default:
                  description: Error object
                  schema:
                    type: object
                    properties:
                      status:
                        type: string
        """.trimIndent()

        val context = DefaultContext.createSwaggerContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations.map { it.pointer.toString() }).containsExactlyInAnyOrder(
            "/paths/~1pets/get/responses/default/schema/properties/status",
            "/paths/~1pets/get/responses/default/schema/properties/status/type"
        )
    }

    @Test
    fun `should return no violation if Problem Details Object is properly used as default response`() {
        val content = """
        openapi: 3.0.1
        info:
          title: Pets API
          version: 1.0.0
        paths:
          "/pets":
            get:
              responses:
                default:
                  description: Good default response.
                  content:
                    application/problem+json:
                      schema:
                        "${'$'}ref": https://zalando.github.io/problem/schema.yaml#/Problem
        """.trimIndent()

        val context = DefaultContext.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should return no violation if Problem Details Object is properly used as default response (OpenAPI 2)`() {
        val content = """
        swagger: '2.0'
        info:
          version: 1.0.0
          title: Pets API
        paths:
          /pets:
            get:
              produces:
                - application/json
                - application/problem+json
              responses:
                default:
                  description: Error object
                  schema:
                    "${'$'}ref": https://zalando.github.io/problem/schema.yaml#/Problem
        """.trimIndent()

        val context = DefaultContext.createSwaggerContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should return no violation when custom reference is used`() {
        val content = """
        openapi: 3.0.1
        info:
          title: Pets API
          version: 1.0.0
        paths:
          "/pets":
            get:
              responses:
                default:
                  description: Good default response.
                  content:
                    application/problem+json:
                      schema:
                        "${'$'}ref": "#/components/schemas/Errors"
        components:
          schemas:
            Errors:
              required:
                - error
              properties:
                error:
                  ${'$'}ref: "#/components/schemas/Error"
            Error:
              required:
                - code
                - title
              properties:
                title:
                  type: "string"
                code:
                  type: "string"
        """.trimIndent()

        val context = DefaultContext.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations).isEmpty()
    }

    @Test
    fun `should return violations when wrong reference is used`() {
        val content = """
        openapi: 3.0.1
        info:
          title: Pets API
          version: 1.0.0
        paths:
          "/pets":
            get:
              responses:
                default:
                  description: Good default response.
                  content:
                    application/problem+json:
                      schema:
                        ${'$'}ref: "#/components/schemas/Problem"
        components:
          schemas:
            Problem:
              description: not a problem detail object as defined in RFC 7807
              properties:
                instance:
                  type: number
                  example: 99
                description:
                  type: string
                  example: many many problems
        """.trimIndent()

        val context = DefaultContext.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations).isNotEmpty
    }
}
