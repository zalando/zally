package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NestedPathsMayBeRootPathsRuleTest {

    private val rule = NestedPathsMayBeRootPathsRule()

    @Test
    fun avoidLinkHeadersValidJson() {
        val swagger = getFixture("api_spp.json")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("/products/{product_id}/updates/{update_id}"))
    }

    @Test
    fun avoidLinkHeadersValidYaml() {
        val swagger = getFixture("api_spa.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}
