package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.swaggerWithDefinitions
import de.zalando.zally.testConfig
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SnakeCaseInPropNameRuleTest {

    private val rule = SnakeCaseInPropNameRule(testConfig)

    @Test
    fun emptySwagger() {
        assertThat(rule.validate(ApiAdapter(OpenAPI()))).isNull()
    }

    @Test
    fun validateNormalProperty() {
        val swagger = swaggerWithDefinitions("ExampleDefinition" to listOf("test_property"))
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun validateMultipleNormalProperties() {
        val swagger = swaggerWithDefinitions("ExampleDefinition" to listOf("test_property", "test_property_two"))
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun validateMultipleNormalPropertiesInMultipleDefinitions() {
        val swagger = swaggerWithDefinitions(
            "ExampleDefinition" to listOf("test_property"),
            "ExampleDefinitionTwo" to listOf("test_property_two")
        )
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }

    @Test
    fun validateFalseProperty() {
        val swagger = swaggerWithDefinitions("ExampleDefinition" to listOf("TEST_PROPERTY"))
        val result = rule.validate(ApiAdapter(swagger))!!
        assertThat(result.description).contains("TEST_PROPERTY")
        assertThat(result.paths).hasSameElementsAs(listOf("#/definitions/ExampleDefinition"))
    }

    @Test
    fun validateMultipleFalsePropertiesInMultipleDefinitions() {
        val swagger = swaggerWithDefinitions(
            "ExampleDefinition" to listOf("TEST_PROPERTY"),
            "ExampleDefinitionTwo" to listOf("test_property_TWO")
        )
        val result = rule.validate(ApiAdapter(swagger))!!
        assertThat(result.description).contains("TEST_PROPERTY", "test_property_TWO")
        assertThat(result.paths).hasSameElementsAs(listOf(
            "#/definitions/ExampleDefinition",
            "#/definitions/ExampleDefinitionTwo")
        )
    }

    @Test
    fun notFireOnWhitelistedProperty() {
        val swagger = swaggerWithDefinitions("ExampleDefinition" to listOf("_links"))
        assertThat(rule.validate(ApiAdapter(swagger))).isNull()
    }
}
