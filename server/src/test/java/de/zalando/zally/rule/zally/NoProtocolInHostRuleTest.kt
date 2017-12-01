package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoProtocolInHostRuleTest {

    private val rule = NoProtocolInHostRule(ZallyRuleSet())

    val expectedViolation = rule.let {
        Violation(it, it.title, "", it.violationType, emptyList())
    }

    @Test
    fun emptySwagger() {
        val swagger = Swagger()
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCase() {
        val swagger = Swagger().apply { host = "google.com" }
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun negativeCaseHttp() {
        val swagger = Swagger().apply { host = "http://google.com" }
        val res = rule.validate(swagger)
        assertThat(res?.copy(description = "")).isEqualTo(expectedViolation)
    }

    @Test
    fun negativeCaseHttps() {
        val swagger = Swagger().apply { host = "https://google.com" }
        val res = rule.validate(swagger)
        assertThat(res?.copy(description = "")).isEqualTo(expectedViolation)
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseSpa() {
        val swagger = getFixture("api_spa.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}
