package de.zalando.zally.rule.zalando

import de.zalando.zally.getFixture
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtensibleEnumRuleTest {

    val rule = ExtensibleEnumRule()

    @Test
    fun returnsNoViolationIfEmptySwagger() {
        assertThat(rule.validate(ApiAdapter(Swagger(), OpenAPI()))).isNull()
    }

    @Test
    fun returnsViolationIfAnEnumInModelProperty() {
        val adapter = getFixture("enum_in_model_property.yaml")
        val expectedViolation = Violation(
                description = "Properties/Parameters [status] are not extensible enums",
                paths = listOf("#/definitions/CrawledAPIDefinition/properties/status"))

        val violation = rule.validate(adapter)

        assertThat(violation).isNotNull()
        assertThat(violation).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsViolationIfAnEnumInRequestParameter() {
        val adapter = getFixture("enum_in_request_parameter.yaml")
        val expectedViolation = Violation(
                description = "Properties/Parameters [lifecycle_state, environment] are not extensible enums",
                paths = listOf("#/paths/apis/{api_id}/versions/GET/parameters/lifecycle_state",
                        "#/paths/apis/GET/parameters/environment"))

        val violation = rule.validate(adapter)

        assertThat(violation).isNotNull()
        assertThat(violation).isEqualTo(expectedViolation)
    }

    @Test
    fun returnsNoViolationIfNoEnums() {
        val adapter = getFixture("no_must_violations.yaml")

        assertThat(rule.validate(adapter)).isNull()
    }
}
