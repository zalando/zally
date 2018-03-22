package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FormatForNumbersRuleTest {

    private val rule = FormatForNumbersRule(testConfig)

    @Test
    fun positiveCase() {
        val adapter = getFixture("formatForNumbersValid.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun negativeCase() {
        val adapter = getFixture("formatForNumbersInvalid.json")
        val result = rule.validate(adapter)!!
        assertThat(result.paths).hasSameElementsAs(listOf("#/parameters/PetFullPrice", "#/definitions/Pet", "/pets"))
        assertThat(result.description).contains("other_price", "full_price", "number_of_legs")
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