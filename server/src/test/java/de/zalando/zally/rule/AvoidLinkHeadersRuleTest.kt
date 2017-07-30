package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.getFixture
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(AvoidLinkHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseSpa() {
        val swagger = getFixture("api_spa.yaml")
        assertThat(AvoidLinkHeadersRule(testConfig).validate(swagger)).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("avoidLinkHeaderRuleInvalid.json")
        val violation = AvoidLinkHeadersRule(testConfig).validate(swagger)!!
        assertThat(violation.violationType).isEqualTo(ViolationType.MUST)
        assertThat(violation.paths).hasSameElementsAs(
                listOf(
                        "/parameters/TestParameter Link",
                        "/products Link",
                        "/product-put-requests/{product_path}/202 Link"))

        assertThat(violation.specPointers).hasSameElementsAs(
                listOf(
                        "/parameters/TestParameter/name",
                        "/paths/~1products/get/parameters/4/name",
                        "/paths/~1product-put-requests~1{product_path}/post/responses/202/headers/Link"))

    }
}
