package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.swaggerWithPaths
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class EverySecondPathLevelParameterRuleTest {

    private val rule = EverySecondPathLevelParameterRule()

    @Test
    fun positiveCase() {
        val swagger = swaggerWithPaths(
            "/some/{param-1}/path/",
            "/another/{param-1}/path/{param-2}/third",
            "/merchant/{merchant_id}/order/{order_id}",
            "/orders")
        assertThat(rule.validate(ApiAdapter(swagger, OpenAPI()))).isNull()
    }

    @Test
    fun negativeCase() {
        val swagger = swaggerWithPaths(
            "/api/some/{param-1}/path/",
            "/another/{param-0}/{param-1}",
            "/okeish-path",
            "/{merchant_id}",
            "/{parcel_id}/info/{update-group}")
        val result = rule.validate(ApiAdapter(swagger, OpenAPI()))!!

        assertThat(result.paths).hasSameElementsAs(listOf(
            "/api/some/{param-1}/path/",
            "/another/{param-0}/{param-1}",
            "/{merchant_id}",
            "/{parcel_id}/info/{update-group}"))
    }

    @Test
    fun positiveCaseSpp() {
        val adapter = getFixture("api_spp.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun negativeCaseSpa() {
        val adapter = getFixture("api_spa.yaml")
        assertThat(rule.validate(adapter)!!.paths).hasSameElementsAs(listOf("/reports/jobs", "/reports/jobs/{job-id}"))
    }
}
