package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    private val rule = AvoidLinkHeadersRule(testConfig)

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
    fun negativeCase() {
        val adapter = getFixture("avoidLinkHeaderRuleInvalid.json")
        val violation = rule.validate(adapter)!!
        assertThat(violation.paths).hasSameElementsAs(
            listOf("/product-put-requests/{product_path} Link", "/products Link"))
    }
}
