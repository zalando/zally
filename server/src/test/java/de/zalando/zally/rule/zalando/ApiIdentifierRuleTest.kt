package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.zalando.zally.rule.ApiAdapter
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.parser.OpenAPIV3Parser
import org.assertj.core.api.Assertions
import org.junit.Test

class ApiIdentifierRuleTest {

    private val rule = ApiIdentifierRule()

    @Test
    fun correctApiIdIsSet() {
        val swagger = withApiId("zally-api")

        Assertions.assertThat(rule.validate(ApiAdapter(null, swagger))).isNull()
    }

    @Test
    fun incorrectIdIsSet() {
        val swagger = withApiId("This?iS//some|Incorrect+&ApI)(id!!!")

        val violation = rule.validate(ApiAdapter(null, swagger))!!

        Assertions.assertThat(violation.paths).hasSameElementsAs(listOf("/info/x-api-id"))
        Assertions.assertThat(violation.description).matches(".*doesn't match.*")
    }

    @Test
    fun noApiIdIsSet() {
        val violation = rule.validate(ApiAdapter(null, OpenAPI()))!!

        Assertions.assertThat(violation.paths).hasSameElementsAs(listOf("/info/x-api-id"))
        Assertions.assertThat(violation.description).matches(".*should be provided.*")
    }

    private fun withApiId(apiId: String): OpenAPI {
        val root = JsonNodeFactory.instance.objectNode()
        root.putObject("info")
            .put("x-api-id", apiId)
        return OpenAPIV3Parser().readWithInfo(root).openAPI
    }
}
