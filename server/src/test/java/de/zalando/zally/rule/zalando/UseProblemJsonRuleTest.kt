package de.zalando.zally.rule.zalando

import de.zalando.zally.getResourceContent
import de.zalando.zally.rule.Context
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UseProblemJsonRuleTest {

    private val rule = UseProblemJsonRule()

    @Test
    fun shouldReportDefaultResponsesWithoutProblemType() {
        val content = """
        openapi: 3.0.0
        info:
          version: 1.0.0
          title: Test
        paths:
          "/bad":
            get:
              responses:
                default:
                  description: Bad default response.
                  content:
                    application/json:
                      schema:
                        type: object
                        properties:
                          status:
                            type: string
          "/good":
            get:
              responses:
                "200":
                  description: Good response.
                  content:
                    application/json:
                      schema:
                        type: object
                default:
                  description: Good default response.
                  content:
                    application/json:
                      schema:
                        "${'$'}ref": https://zalando.github.io/problem/schema.yaml#/Problem
            """.trimIndent()

        val context = Context.createOpenApiContext(content)!!
        val violations = rule.validate(context)

        assertThat(violations.map { it.pointer }).containsExactlyInAnyOrder(
            "#/paths/~1bad/get/responses/default/content/application~1json/schema/properties/status",
            "#/paths/~1bad/get/responses/default/content/application~1json/schema/properties/status/type"
        )
    }

    @Test
    fun shouldReturnNoViolationsWhenErrorsReferencingToProblemJson() {
        val content = getResourceContent("problem_json.yaml")
        val context = Context.createSwaggerContext(content)!!
        assertThat(rule.validate(context)).isEmpty()
    }

    @Test
    fun shouldReturnViolationsWhenErrorsReferencingToProblemJsonButNotProducingJson() {
        val content = getResourceContent("problem_json_not_produces_json.yaml")
        val context = Context.createSwaggerContext(content)!!
        val violations = rule.validate(context)
        assertThat(violations).allMatch {
            it.description.endsWith("application/json.") &&
                it.pointer?.endsWith("/schema")?.or(
                    it.pointer?.matches(Regex("^#/definitions/.*Problem$")) ?: false
                ) ?: false
        }
    }

    @Test
    fun shouldReturnNoViolationsWhenOperationsAreProducingJson() {
        val content = getResourceContent("problem_json_operations_produce_json.yaml")
        val context = Context.createSwaggerContext(content)!!
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun shouldReturnNoViolationsWhenCustomReferenceIsUsed() {
        val content = getResourceContent("api_tinbox.yaml")
        val context = Context.createSwaggerContext(content)!!
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun shouldReturnViolationsWhenNoReferenceIsUsed() {
        val content = getResourceContent("api_spp.json")
        val context = Context.createSwaggerContext(content)!!
        val violations = rule.validate(context)
        assertThat(violations).isNotEmpty
    }

    @Test
    fun shouldNotThrowExOnSchemasWithReferencesToEmptyDefinitions() {
        val content = getResourceContent("missing_definitions.yaml")
        val context = Context.createSwaggerContext(content)!!
        val violations = rule.validate(context)
        assertThat(violations).isNotEmpty
    }
}
