package de.zalando.zally.rule.zally

import de.zalando.zally.getFixture
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NoUnusedDefinitionsRuleTest {
    private val rule = NoUnusedDefinitionsRule()

    @Test
    fun positiveCase() {
        assertThat(rule.validate(getFixture("unusedDefinitionsValid.json"))).isNull()
    }

    @Test
    fun negativeCase() {
        val results = rule.validate(getFixture("unusedDefinitionsInvalid.json"))!!.paths
        assertThat(results).hasSameElementsAs(listOf(
            "/definitions/PetName",
            "/parameters/FlowId"
        ))
    }

    @Test
    fun emptySwaggerShouldPass() {
        val swagger = Swagger()
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun positiveCaseTinbox() {
        val swagger = getFixture("api_tinbox.yaml")
        assertThat(rule.validate(swagger)).isNull()
    }
}
