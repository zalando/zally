package de.zalando.zally.rule

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LimitNumberOfResourcesRuleTest {

    private val rule = LimitNumberOfResourcesRule(ZalandoRuleSet(), testConfig)

    @Test
    fun positiveCase() {
        val swagger = getFixture("limitNumberOfResourcesValid.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("limitNumberOfResourcesInvalid.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf(
            "/items",
            "/items/{item_id}",
            "/items10",
            "/items3",
            "/items4",
            "/items5",
            "/items6",
            "/items7",
            "/items8",
            "/items9"))
    }
}
