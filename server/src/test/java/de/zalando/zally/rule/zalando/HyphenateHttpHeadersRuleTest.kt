package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.swaggerWithHeaderParams
import de.zalando.zally.testConfig
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HyphenateHttpHeadersRuleTest {

    private val rule = HyphenateHttpHeadersRule(ZalandoRuleSet(), testConfig)

    @Test
    fun simplePositiveCase() {
        val swagger = swaggerWithHeaderParams("Right-Name")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun simpleNegativeCase() {
        val swagger = swaggerWithHeaderParams("CamelCaseName")
        val result = rule.validate(swagger)!!
        assertThat(result.paths).hasSameElementsAs(listOf("parameters CamelCaseName"))
    }

    @Test
    fun mustAcceptValuesFromWhitelist() {
        val swagger = swaggerWithHeaderParams("ETag", "X-Trace-ID")
        assertThat(rule.validate(swagger)).isNull()
    }

    @Test
    fun emptySwaggerShouldPass() {
        val swagger = Swagger()
        swagger.parameters = HashMap<String, Parameter>()
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
