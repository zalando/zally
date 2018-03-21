package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoUnusedDefinitionsRuleTest {
    private val rule = NoUnusedDefinitionsRule()

    @Test
    fun positiveCase() {
        val api = getFixture("unusedDefinitionsValid.json")
        assertThat(rule.validate(ApiAdapter(api))).isNull()
    }

    @Test
    fun negativeCase() {
        val api = getFixture("unusedDefinitionsInvalid.json")
        val results = rule.validate(ApiAdapter(api))!!.paths
        assertThat(results).hasSameElementsAs(listOf(
            "#/definitions/PetName",
            "#/parameters/FlowId"
        ))
    }

    @Test
    fun emptySwaggerShouldPass() {
        assertThat(rule.validate(ApiAdapter(OpenAPI()))).isNull()
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