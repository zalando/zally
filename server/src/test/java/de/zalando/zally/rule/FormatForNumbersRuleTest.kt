package de.zalando.zally.rule

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatForNumbersRuleTest {

    @Test
    fun positiveCase() {
        val swagger = getFixture("formatForNumbersValid.json")
        assertThat(FormatForNumbersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("formatForNumbersInvalid.json")
        val result = FormatForNumbersRule(testConfig).validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("#/parameters/PetFullPrice", "#/definitions/Pet", "/pets"))
        assertThat(result.description).contains("other_price", "full_price", "number_of_legs")
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(FormatForNumbersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(FormatForNumbersRule(testConfig).validate(swagger)).isNull()
    }
}