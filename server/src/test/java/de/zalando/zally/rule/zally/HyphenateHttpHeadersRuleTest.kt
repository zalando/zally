package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.swaggerWithHeaderParams
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HyphenateHttpHeadersRuleTest {

    private val rule = HyphenateHttpHeadersRule(testConfig)

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithHeaderParams("Right-Name")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun simplePositiveCamelCase() {
        // CamelCaseName IS a valid 'hypenated' header, it just has a single term
        val swagger = swaggerWithHeaderParams("CamelCaseName")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun mustAcceptValuesFromWhitelist() {
        val swagger = swaggerWithHeaderParams("ETag", "X-Trace-ID")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun emptySwaggerShouldPass() {
        assertThat(rule.validate(ApiAdapter(Swagger(), OpenAPI()))).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val adapter = getFixture("api_spp.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val adapter = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun issue572RateLimitHeadersAreAccepted() {
        val swagger = swaggerWithHeaderParams("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }
}
