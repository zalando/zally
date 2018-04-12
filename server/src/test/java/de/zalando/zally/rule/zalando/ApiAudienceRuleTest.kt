package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.Context
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApiAudienceRuleTest {

    private val rule = ApiAudienceRule(testConfig)

    @Test
    fun correctApiAudienceIsSet() {
        val context = withAudience("company-internal")

        assertThat(rule.validate(context)).isNull()
    }

    @Test
    fun incorrectAudienceIsSet() {
        val context = withAudience("not-existing-audience")
        val violation = rule.validate(context)

        assertThat(violation?.pointer).isEqualTo("#/info/x-audience")
        assertThat(violation?.description).matches(".*doesn't match.*")
    }

    @Test
    fun noApiAudienceIsSet() {
        val context = withAudience("null")
        val violation = rule.validate(context)

        assertThat(violation?.pointer).isEqualTo("#/info/x-audience")
        assertThat(violation?.description).matches(".*Audience must be provided.*")
    }

    private fun withAudience(apiAudience: String): Context {
        val content = """
            openapi: '3.0.0'
            info:
              x-audience: $apiAudience
            paths: {}
            """.trimIndent()

        return Context.createOpenApiContext(content)!!
    }
}
