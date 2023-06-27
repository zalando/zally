package de.zalando.zally.ruleset.sbb

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.rule.api.Context
import de.zalando.zally.test.ZallyAssertions.assertThat
import org.junit.Test

class ApiIdentifierRuleTest {

    private val rule = ApiIdentifierRule()

    @Test
    fun correctApiIdIsSet() {
        val context = withApiId("zally-api")

        val violation = rule.validate(context)

        assertThat(violation)
            .isNull()
    }

    @Test
    fun incorrectIdIsSet() {
        val context = withApiId("This?iS//some|Incorrect+&ApI)(id!!!")
        val violation = rule.validate(context)!!

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun noApiIdIsSet() {
        val context = withApiId("null")
        val violation = rule.validate(context)!!

        assertThat(violation)
            .pointerEqualTo("/info/x-api-id")
            .descriptionMatches(".*should be provided.*")
    }

    private fun withApiId(apiId: String): Context {
        val content = """
            openapi: '3.0.0'
            info:
              x-api-id: $apiId
            paths: {}
            """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }
}
