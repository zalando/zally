package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.Context
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApiIdentifierRuleTest {

    private val rule = ApiIdentifierRule()

    @Test
    fun correctApiIdIsSet() {
        val context = withApiId("zally-api")

        assertThat(rule.validate(context)).isNull()
    }

    @Test
    fun incorrectIdIsSet() {
        val context = withApiId("This?iS//some|Incorrect+&ApI)(id!!!")
        val violation = rule.validate(context)!!

        assertThat(violation.pointer).isEqualTo("#/info/x-api-id")
        assertThat(violation.description).matches(".*doesn't match.*")
    }

    @Test
    fun noApiIdIsSet() {
        val context = withApiId("null")
        val violation = rule.validate(context)!!

        assertThat(violation.pointer).isEqualTo("#/info/x-api-id")
        assertThat(violation.description).matches(".*should be provided.*")
    }

    private fun withApiId(apiId: String): Context {
        val content = """
            openapi: '3.0.0'
            info:
              x-api-id: $apiId
            paths: {}
            """.trimIndent()

        return Context.createOpenApiContext(content)!!
    }
}
