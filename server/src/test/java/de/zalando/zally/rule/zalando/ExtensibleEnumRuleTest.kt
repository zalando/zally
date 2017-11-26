package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensibleEnumRuleTest {

    val rule = ExtensibleEnumRule(ZalandoRuleSet())

    @Test
    fun returnsNoViolationIfEmptySwagger() {
        assertThat(rule.validate(Swagger())).isNull()
    }

    @Test
    fun returnsViolationIfAnEnumInModelProperty() {
        val swagger = getFixture("enum_in_model_property.yaml")

        val violation = rule.validate(swagger)

        assertThat(violation)
                .hasFieldOrPropertyWithValue("description", "Properties/Parameters [status] are not extensible enums")
                .hasFieldOrPropertyWithValue("paths", listOf("#/definitions/CrawledAPIDefinition/properties/status"))
    }

    @Test
    fun returnsViolationIfAnEnumInRequestParameter() {
        val swagger = getFixture("enum_in_request_parameter.yaml")

        val violation = rule.validate(swagger)

        assertThat(violation)
                .hasFieldOrPropertyWithValue("description", "Properties/Parameters [lifecycle_state, environment] are not extensible enums")
                .hasFieldOrPropertyWithValue("paths", listOf(
                        "#/paths/apis/{api_id}/versions/GET/parameters/lifecycle_state",
                        "#/paths/apis/GET/parameters/environment"))
    }

    @Test
    fun returnsNoViolationIfNoEnums() {
        val swagger = getFixture("no_must_violations.yaml")

        assertThat(rule.validate(swagger)).isNull()
    }

}
