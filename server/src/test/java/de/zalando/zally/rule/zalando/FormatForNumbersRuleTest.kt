package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatForNumbersRuleTest {

    private val rule = FormatForNumbersRule(testConfig)

    @Test
    fun positiveCase() {
        val swagger = getFixture("formatForNumbersValid.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("formatForNumbersInvalid.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("#/parameters/PetFullPrice", "#/definitions/Pet", "/pets"))
        assertThat(result.description).contains("other_price", "full_price", "number_of_legs")
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}