package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    private val rule = AvoidLinkHeadersRule(ZalandoRuleSet(), testConfig)

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

    @Test
    fun negativeCase() {
        val swagger = getFixture("avoidLinkHeaderRuleInvalid.json")
        val violation = rule.validate(swagger)!!
        assertThat(violation.violationType).isEqualTo(ViolationType.MUST)
        assertThat(violation.paths).hasSameElementsAs(
            listOf("/product-put-requests/{product_path} Link", "/products Link"))
    }
}
