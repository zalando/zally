package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoUnusedDefinitionsRuleTest {
    private val rule = NoUnusedDefinitionsRule()

    @Test
    fun positiveCase() {
        val adapter = getFixture("unusedDefinitionsValid.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun negativeCase() {
        val adapter = getFixture("unusedDefinitionsInvalid.json")
        val results = rule.validate(adapter)!!.paths
        assertThat(results).hasSameElementsAs(listOf(
            "#/definitions/PetName",
            "#/parameters/FlowId"
        ))
    }

    @Test
    fun emptySwaggerShouldPass() {
        assertThat(rule.validate(ApiAdapter(Swagger(), OpenAPI()))).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val adapter = getFixture("api_spp.json")
        assertThat(rule.validate(adapter)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val adapter = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(adapter)).isNull()
    }
}