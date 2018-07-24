package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.getContextFromFixture
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidLinkHeadersRuleTest {

    private val rule = AvoidLinkHeadersRule(testConfig)

    @Test
    fun positiveCaseSpp() {
        val context = getContextFromFixture("api_spp.json")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun positiveCaseSpa() {
        val context = getContextFromFixture("api_spa.yaml")
        val violations = rule.validate(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun negativeCase() {
        val context = getContextFromFixture("avoidLinkHeaderRuleInvalid.json")
        val violations = rule.validate(context)
        assertThat(violations).hasSameElementsAs(listOf(
            v("/paths/~1products/get/parameters/4"),
            v("/paths/~1product-put-requests~1{product_path}/post/responses/202/headers/Link")
        ))
    }

    private fun v(pointer: String) = Violation(
        description = "Do Not Use Link Headers with JSON entities",
        pointer = JsonPointer.compile(pointer)
    )
}
