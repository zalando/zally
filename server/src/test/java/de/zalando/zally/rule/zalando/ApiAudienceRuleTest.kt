package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.DefaultContextFactory
import de.zalando.zally.rule.ZallyAssertions.assertThat
import de.zalando.zally.rule.api.Context
import de.zalando.zally.testConfig
import org.junit.Test

class ApiAudienceRuleTest {

    private val rule = ApiAudienceRule(testConfig)

    @Test
    fun correctApiAudienceIsSet() {
        val context = withAudience("company-internal")

        val violation = rule.validate(context)

        assertThat(violation)
            .isNull()
    }

    @Test
    fun incorrectAudienceIsSet() {
        val context = withAudience("not-existing-audience")
        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-audience")
            .descriptionMatches(".*doesn't match.*")
    }

    @Test
    fun noApiAudienceIsSet() {
        val context = withAudience("null")
        val violation = rule.validate(context)

        assertThat(violation)
            .pointerEqualTo("/info/x-audience")
            .descriptionMatches(".*Audience must be provided.*")
    }

    private fun withAudience(apiAudience: String): Context {
        val content = """
            openapi: '3.0.0'
            info:
              x-audience: $apiAudience
              title: Lorem Ipsum
              version: 1.0.0
            paths: {}
            """.trimIndent()

        return DefaultContextFactory().getOpenApiContext(content)
    }
}
