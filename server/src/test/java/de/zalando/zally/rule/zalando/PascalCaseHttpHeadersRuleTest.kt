package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.swaggerWithHeaderParams
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PascalCaseHttpHeadersRuleTest {

    private val rule = PascalCaseHttpHeadersRule(testConfig)

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithHeaderParams("Right-Name")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun simpleNegativeCase() {
        val swagger = swaggerWithHeaderParams("kebap-case-name")
        val result = rule.validate(ApiAdapter(swagger, OpenAPI()))!!
        assertThat(result.paths).hasSameElementsAs(listOf("parameters kebap-case-name"))
    }

    @Test
    fun mustAcceptETag() {
        val swagger = swaggerWithHeaderParams("ETag")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun mustAcceptZalandoHeaders() {
        val swagger = swaggerWithHeaderParams("X-Flow-ID", "X-UID", "X-Tenant-ID", "X-Sales-Channel", "X-Frontend-Type",
                "X-Device-Type", "X-Device-OS", "X-App-Domain")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun mustAcceptRateLimitHeaders() {
        val swagger = swaggerWithHeaderParams("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun mustAcceptDigits() {
        val swagger = swaggerWithHeaderParams("X-P1n-Id")
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
}
