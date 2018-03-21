package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SnakeCaseForQueryParamsRuleTest {

    private val validSwagger = getFixture("snakeCaseForQueryParamsValid.json")
    private val invalidSwaggerWithLocalParam = getFixture("snakeCaseForQueryParamsInvalidLocalParam.json")
    private val invalidSwaggerWIthInternalRef = getFixture("snakeCaseForQueryParamsInvalidInternalRef.json")
    private val invalidSwaggerWithExternalRef = getFixture("snakeCaseForQueryParamsInvalidExternalRef.json")

    private val rule = SnakeCaseForQueryParamsRule()

    @Test
    fun shouldFindNoViolations() {
        assertThat(rule.validate(ApiAdapter(validSwagger))).isNull()
    }

    @Test
    fun shouldFindViolationsInLocalRef() {
        val result = rule.validate(ApiAdapter(invalidSwaggerWithLocalParam))!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInInternalRef() {
        val result = rule.validate(ApiAdapter(invalidSwaggerWIthInternalRef))!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInExternalRef() {
        val result = rule.validate(ApiAdapter(invalidSwaggerWithExternalRef))!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }
}
