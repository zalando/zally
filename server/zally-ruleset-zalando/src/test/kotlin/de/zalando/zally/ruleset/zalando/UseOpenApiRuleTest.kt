package de.zalando.zally.ruleset.zalando

import com.typesafe.config.ConfigFactory
import de.zalando.zally.core.DefaultContext
import de.zalando.zally.core.ObjectTreeReader
import de.zalando.zally.core.rulesConfig
import de.zalando.zally.ruleset.zalando.util.getResourceJson
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class UseOpenApiRuleTest {

    private val rule = UseOpenApiRule(rulesConfig)

    @Test
    fun shouldNotFailOnCorrectYaml() {
        listOf("swagger2_petstore_expanded.yaml", "openapi3_petstore.yaml").forEach {
            val json = getResourceJson(it)
            val validations = rule.validateSchema(json)
            assertThat(validations).hasSize(0)
        }
    }

    @Test
    fun shouldNotFailOnCorrectJson() {
        listOf("swagger2_petstore_expanded.json", "openapi3_petstore.json").forEach {
            val json = getResourceJson(it)
            val validations = rule.validateSchema(json)
            assertThat(validations).hasSize(0)
        }
    }

    @Test
    fun shouldReportInvalidYaml() {
        val json = ObjectTreeReader().read("foo: bar")
        val validations = rule.validateSchema(json)
        assertThat(validations).isNotEmpty
        assertThat(validations).anyMatch { it.description.matches(Regex(".*Object has missing required properties.*")) }
        assertThat(validations).anyMatch { it.description.matches(Regex(".*Object instance has properties.*")) }
    }

    @Test
    fun shouldLoadSchemaFromResourceIfUrlNotSpecified() {
        val config = ConfigFactory.parseString(
            """
        UseOpenApiRule {
             // swagger_schema_url not defined
        }
        """
        )

        val json = ObjectTreeReader().read("foo: bar")
        val validations = UseOpenApiRule(config).validateSchema(json)
        assertThat(validations).isNotEmpty
    }

    @Test
    fun shouldLoadSchemaFromResourceIfLoadFromUrlFailed() {
        val config = ConfigFactory.parseString(
            """
        UseOpenApiRule {
             swagger_schema_url: "http://localhost/random_url.html"
        }
        """
        )

        val json = ObjectTreeReader().read("foo: bar")
        val validations = UseOpenApiRule(config).validateSchema(json)
        assertThat(validations).isNotEmpty
    }

    @Test
    fun `checkIfTheFormatIsYaml should return a violation if JSON is used`() {
        val context = DefaultContext("\t\r\n{\"openapi\": \"3.0.1\"}\t\r\n", OpenAPI(), null)

        val violation = rule.checkIfTheFormatIsYAML(context)

        assertThat(violation).isNotNull
        assertThat(violation!!.description).containsPattern(".*must use YAML format.*")
    }

    @Test
    fun `checkIfTheFormatIsYaml should return no violation if YAML is used`() {
        val context = DefaultContext("openapi: 3.0.1", OpenAPI(), null)

        val violation = rule.checkIfTheFormatIsYAML(context)

        assertThat(violation).isNull()
    }

    @Test
    fun `validateSchema should return no violation for valid OpenAPI 3 specification`() {
        val jsonNode = ObjectTreeReader().read(
            """
            openapi: 3.0.1
            info:
              title: "Minimal API"
              version: "1.0.0"
            paths: {}
        """.trimIndent()
        )

        val violations = rule.validateSchema(jsonNode)

        assertThat(violations).isEmpty()
    }
}
