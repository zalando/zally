package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.swaggerWithPaths
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AvoidTrailingSlashesRuleTest {

    private val rule = AvoidTrailingSlashesRule()

    @Test
    fun emptySwagger() {
        assertThat(rule.validate(ApiAdapter(OpenAPI()))).isNull()
    }

    @Test
    fun positiveCase() {
        val testAPI = swaggerWithPaths("/api/test-api")
        assertThat(rule.validate(ApiAdapter(testAPI))).isNull()
    }

    @Test
    fun negativeCase() {
        val testAPI = swaggerWithPaths("/api/test-api/", "/api/test", "/some/other/path", "/long/bad/path/with/slash/")
        assertThat(rule.validate(ApiAdapter(testAPI))!!.paths).hasSameElementsAs(
            listOf("/api/test-api/", "/long/bad/path/with/slash/"))
    }
}
