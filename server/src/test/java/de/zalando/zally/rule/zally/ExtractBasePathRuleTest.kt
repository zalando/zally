package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.swaggerWithPaths
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtractBasePathRuleTest {
    val DESC_PATTERN = "All paths start with prefix '%s'. This prefix could be part of base path."

    private val rule = ExtractBasePathRule()

    @Test
    fun validateEmptyPath() {
        assertThat(rule.validate(ApiAdapter(OpenAPI()))).isNull()
    }

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithPaths("/orders/{order_id}", "/orders/{updates}", "/merchants")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun singlePathShouldPass() {
        val swagger = swaggerWithPaths("/orders/{order_id}")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun simpleNegativeCase() {
        val swagger = swaggerWithPaths(
            "/shipment/{shipment_id}",
            "/shipment/{shipment_id}/status",
            "/shipment/{shipment_id}/details"
        )
        val rule = rule
        val expected = Violation(DESC_PATTERN.format("/shipment"),
                emptyList())
        assertThat(rule.validate(ApiAdapter(swagger))).isEqualTo(expected)
    }

    @Test
    fun multipleResourceNegativeCase() {
        val swagger = swaggerWithPaths(
            "/queue/models/configs/{config-id}",
            "/queue/models/",
            "/queue/models/{model-id}",
            "/queue/models/summaries"
        )
        val rule = rule
        val expected = Violation(DESC_PATTERN.format("/queue/models"),
                emptyList())
        assertThat(rule.validate(ApiAdapter(swagger))).isEqualTo(expected)
    }

    @Test
    fun shouldMatchWholeSubresource() {
        val swagger = swaggerWithPaths(
            "/api/{api_id}/deployments",
            "/api/{api_id}/",
            "/applications/{app_id}",
            "/applications/"
        )
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }
}
