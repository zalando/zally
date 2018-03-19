package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.testConfig
import de.zalando.zally.validateSwaggerContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class LimitNumberOfSubresourcesRuleTest {
    val ruleConfig = testConfig

    private val rule = LimitNumberOfSubresourcesRule(ruleConfig)

    @Test
    fun positiveCase() {
        val swagger = getFixture("limitNumberOfSubresourcesValid.json")

        val violation: Violation? = validateSwaggerContext(swagger, rule, rule::validate)

        assertThat(violation).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = getFixture("limitNumberOfSubresourcesInvalid.json")

        val violation: Violation? = validateSwaggerContext(swagger, rule, rule::validate)

        assertThat(violation?.paths).hasSameElementsAs(
            listOf("paths: /items/{some_id}/item_level_1/{level1_id}/item-level-2/{level2_id}/item-level-3/{level3_id}/item-level-4: 4 sub-resources")
        )
    }

    @Test
    fun positiveCaseDueToIgnoreExtension() {
        val swagger = getFixture("limitNumberOfSubresourcesInvalidButIgnored.json")

        val violation: Violation? = validateSwaggerContext(swagger, rule, rule::validate)

        assertThat(violation).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")

        val violation: Violation? = validateSwaggerContext(swagger, rule, rule::validate)

        assertThat(violation).isNull()
    }

    @Test
    fun positiveCaseSpa() {
        val swagger = getFixture("api_spa.yaml")

        val violation: Violation? = validateSwaggerContext(swagger, rule, rule::validate)

        assertThat(violation).isNull()
    }
}
