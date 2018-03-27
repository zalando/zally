package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.core.models.ParseOptions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoProtocolInHostRuleTest {

    private val rule = NoProtocolInHostRule()

    private val expectedViolation = rule.let {
        Violation("", emptyList())
    }

    @Test
    fun emptySwagger() {
        assertThat(rule.validate(ApiAdapter(Swagger(), OpenAPI()))).isNull()
    }

    @Test
    fun positiveCase() {
        val swagger = Swagger().apply { host = "google.com" }
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun negativeCaseHttp() {
        val swagger = Swagger().apply { host = "http://google.com" }
        val res = rule.validate(ApiAdapter(swagger, OpenAPI()))
        assertThat(res?.copy(description = "")).isEqualTo(expectedViolation)
    }

    @Test
    fun negativeCaseHttps() {
        val swagger = Swagger().apply { host = "https://google.com" }
        val res = rule.validate(ApiAdapter(swagger, OpenAPI()))
        assertThat(res?.copy(description = "")).isEqualTo(expectedViolation)
    }

    @Test
    fun positiveCaseSpp() {
        val adapter = getFixture("api_spp.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun positiveCaseSpa() {
        val adapter = getFixture("api_spa.yaml")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun checkV3ServerUrls() {
        val parser = OpenAPIV3Parser()
                .readContents("""
                    openapi: "3.0.0"
                    servers:
                      - url: http://google.com
                      - url: https://google.com
                      - url: /some-path
                    """.trimMargin(), null, ParseOptions())

        assertThat(rule.validate(ApiAdapter(null, parser.openAPI))).isNull()

    }
}
