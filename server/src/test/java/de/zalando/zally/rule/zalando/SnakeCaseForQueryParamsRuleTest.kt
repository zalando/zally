package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SnakeCaseForQueryParamsRuleTest {

    private val rule = SnakeCaseForQueryParamsRule()

    @Test
    fun shouldFindNoViolations() {
        val swagger = getFixture("snakeCaseForQueryParamsValid.json")
        val result = rule.validate(swagger)
        assertThat(result).isNull()
    }

    @Test
    fun shouldFindViolationsInLocalRef() {
        val swagger = getFixture("snakeCaseForQueryParamsInvalidLocalParam.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInInternalRef() {
        val swagger = getFixture("snakeCaseForQueryParamsInvalidInternalRef.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }

    @Test
    fun shouldFindViolationsInExternalRef() {
        val swagger = getFixture("snakeCaseForQueryParamsInvalidExternalRef.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/items GET"))
    }
}
