package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class Use429HeaderForRateLimitRuleTest {
    private val rule = Use429HeaderForRateLimitRule(ZalandoRuleSet())

    @Test
    fun positiveCase() {
        val swagger = getFixture("use429HeadersForRateLimitValid.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("use429HeadersForRateLimitInvalidHeader.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/pets GET 429", "/pets POST 429", "/pets PUT 429"))
    }

    @Test
    fun positiveCaseSpa() {
        val swagger = getFixture("api_spa.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}
