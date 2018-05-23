package de.zalando.zally.rule.zalando

import com.typesafe.config.ConfigFactory
import de.zalando.zally.getResourceJson
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.testConfig
import org.assertj.core.api.Assertions.assertThat
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
}
