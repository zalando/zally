package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import com.typesafe.config.ConfigFactory
import de.zalando.zally.getResourceJson
import de.zalando.zally.rule.DefaultContext
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.api.ParsingMessage
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.testConfig
import io.swagger.v3.oas.models.OpenAPI
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.Test

class UseOpenApiRuleTest {

    private val rule = UseOpenApiRule(testConfig)

    @Test
    fun shouldNotFailOnCorrectYaml() {
        listOf("swagger2_petstore_expanded.yaml", "openapi3_petstore.yaml").forEach {
            val json = getResourceJson(it)
            val validations = rule.validate(json)
            assertThat(validations).hasSize(0)
        }
    }

    @Test
    fun shouldNotFailOnCorrectJson() {
        listOf("swagger2_petstore_expanded.json", "openapi3_petstore.json").forEach {
            val json = getResourceJson(it)
            val validations = rule.validate(json)
            assertThat(validations).hasSize(0)
        }
    }

    @Test
    fun shouldReportInvalidYaml() {
        val json = ObjectTreeReader().read("foo: bar")
        val validations = rule.validate(json)
        assertThat(validations).isNotEmpty
        assertThat(validations).allMatch { it.description.matches(Regex("^Does not match.*")) }
    }

    @Test
    fun shouldLoadSchemaFromResourceIfUrlNotSpecified() {
        val config = ConfigFactory.parseString("""
        UseOpenApiRule {
             // swagger_schema_url not defined
        }
        """)

        val json = ObjectTreeReader().read("foo: bar")
        val validations = UseOpenApiRule(config).validate(json)
        assertThat(validations).isNotEmpty
    }

    @Test
    fun shouldLoadSchemaFromResourceIfLoadFromUrlFailed() {
        val config = ConfigFactory.parseString("""
        UseOpenApiRule {
             swagger_schema_url: "http://localhost/random_url.html"
        }
        """)

        val json = ObjectTreeReader().read("foo: bar")
        val validations = UseOpenApiRule(config).validate(json)
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
    fun `reportParsingMessagesAsHints should return no violation if no parsing message are present`() {
        val context = DefaultContext("openapi: 3.0.1", OpenAPI(), null, emptyList())
        val violations = rule.reportParsingMessagesAsHints(context)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `reportParsingMessagesAsHints should return violations representing the parsing messages`() {
        val parsingMessages = listOf(
            ParsingMessage("Foo", JsonPointer.compile("/paths/foo")),
            ParsingMessage("Bar", JsonPointer.compile("/paths/bar"))
        )
        val context = DefaultContext("openapi: 3.0.1", OpenAPI(), null, parsingMessages)
        val violations = rule.reportParsingMessagesAsHints(context)
        assertThat(violations).hasSameElementsAs(listOf(
            Violation("Foo", JsonPointer.compile("/paths/foo")),
            Violation("Bar", JsonPointer.compile("/paths/bar"))
        ))
    }
}
