package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SuccessResponseAsJsonObjectRuleTest {

    private val validSwagger = getFixture("successResponseAsJsonObjectValid.json")
    private val invalidSwagger = getFixture("successResponseAsJsonObjectInvalid.json")
    private val npeSwagger = getFixture("sample_swagger_api.yaml")

    private val rule = SuccessResponseAsJsonObjectRule()

    @Test
    fun positiveCase() {
        assertThat(rule.validate(ApiAdapter(validSwagger))).isNull()
    }

    @Test
    fun negativeCase() {
        val result = rule.validate(ApiAdapter(invalidSwagger))!!
        assertThat(result.paths).hasSameElementsAs(listOf("/pets GET 200", "/pets POST 200"))
    }

    @Test
    fun positiveCaseSpp() {
        val swagger = getFixture("api_spp.json")
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun npeBug() {
        assertThat(rule.validate(ApiAdapter(npeSwagger))).isNotNull()
    }
}
