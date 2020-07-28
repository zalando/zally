package org.zalando.zally.ruleset.zalando

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.test.ZallyAssertions

class UseProblemJsonRuleTest {

    private val rule = UseProblemJsonRule()

    private val description = "Operations should return problem JSON when any problem occurs during processing " +
        "whether caused by client or server."

    @Test
    fun `should return violation if wrong media type is used as the default response`() {
        @Language("YAML")
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

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo("$description Media type have to be 'application/problem+json'")
            .pointersEqualTo("/paths/~1pets/get/responses/default/content/application~1json")
    }

    @Test
    fun `should return violation if a wrong schema is used for Problem Details Object`() {
        @Language("YAML")
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
                      description: Lorem Ipsum
                      content:
                        application/problem+json:
                          schema:
                            type: object
                            properties:
                              status:
                                type: string
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo(
                "$description Object has missing required properties ([\"exclusiveMaximum\",\"format\",\"maximum\",\"minimum\"])",
                "$description Instance value (\"string\") not found in enum (possible values: [\"integer\"])"
            )
            .pointersEqualTo(
                "/paths/~1bad/get/responses/default/content/application~1problem+json/schema/properties/status",
                "/paths/~1bad/get/responses/default/content/application~1problem+json/schema/properties/status/type"
            )
    }

    @Test
    fun `should return violation if a incorrect schema is used for Problem Details Object (OpenAPI 2)`() {
        @Language("YAML")
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

        val context = DefaultContextFactory().getSwaggerContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo(
                "$description Object has missing required properties ([\"exclusiveMaximum\",\"format\",\"maximum\",\"minimum\"])",
                "$description Instance value (\"string\") not found in enum (possible values: [\"integer\"])"
            )
            .pointersEqualTo(
                "/paths/~1pets/get/responses/default/schema/properties/status",
                "/paths/~1pets/get/responses/default/schema/properties/status/type"
            )
    }

    @Test
    fun `should return no violation if Problem Details Object is properly used as default response`() {
        @Language("YAML")
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

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `should return no violation if Problem Details Object is properly used as default response (OpenAPI 2)`() {
        @Language("YAML")
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
                        "${'$'}ref": #/definitions/Problem
            definitions:
              Problem:
                type: object
                properties:
                  type:
                    type: string
                    format: uri
                    description: |
                      An absolute URI that identifies the problem type.  When dereferenced,
                      it SHOULD provide human-readable documentation for the problem type
                      (e.g., using HTML).
                    default: 'about:blank'
                    example: 'https://zalando.github.io/problem/constraint-violation'
                  title:
                    type: string
                    description: |
                      A short, summary of the problem type. Written in english and readable
                      for engineers (usually not suited for non technical stakeholders and
                      not localized); example: Service Unavailable
                  status:
                    type: integer
                    format: int32
                    description: |
                      The HTTP status code generated by the origin server for this occurrence
                      of the problem.
                    minimum: 100
                    maximum: 600
                    exclusiveMaximum: true
                    example: 503
                  detail:
                    type: string
                    description: |
                      A human readable explanation specific to this occurrence of the
                      problem.
                    example: Connection to database timed out
                  instance:
                    type: string
                    format: uri
                    description: |
                      An absolute URI that identifies the specific occurrence of the problem.
                      It may or may not yield further information if dereferenced.
        """.trimIndent()

        val context = DefaultContextFactory().getSwaggerContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `should return no violation when custom reference is used`() {
        @Language("YAML")
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

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .isEmpty()
    }

    @Test
    fun `should return violations when wrong reference is used`() {
        @Language("YAML")
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

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)

        ZallyAssertions
            .assertThat(violations)
            .descriptionsEqualTo(
                "$description Object has missing required properties ([\"format\"])",
                "$description Instance value (\"number\") not found in enum (possible values: [\"string\"])"
            )
            .pointersEqualTo(
                "/components/schemas/Problem/properties/instance",
                "/components/schemas/Problem/properties/instance/type"
            )
    }

    @Test
    fun `should return no violation if date and time properties are used in the Problem object schema`() {
        @Language("YAML")
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
                      description: Lorem Ipsum
                      content:
                        application/problem+json:
                          schema:
                            type: object
                            allOf: 
                            - "$\ref": https://zalando.github.io/problem/schema.yaml#/Problem
                            - type: object
                              properties:
                                created:
                                  type: string
                                  format: date-time
                                  example: "2020-05-14T14:22:01Z" 
        """.trimIndent()

        val context = DefaultContextFactory().getOpenApiContext(content)
        val violations = rule.validate(context)
        ZallyAssertions.assertThat(violations).isEmpty()
    }
}
