package org.zalando.zally.ruleset.sbb

import com.typesafe.config.ConfigFactory
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test
import org.zalando.zally.core.ObjectTreeReader
import org.zalando.zally.core.rulesConfig
import org.zalando.zally.ruleset.zalando.util.getResourceJson

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

    @Test
    fun `validateSchema should return no violation for valid OpenAPI 3 specification with uri-reference`() {
        @Language("YAML")
        val jsonNode = ObjectTreeReader().read(
            """
            openapi: 3.0.0
            info:
              title: Example API with contact url
              version: 1.0.0
              contact:
                url: http://example.com
            paths:
              /path:
                post:
                  responses:
                    '200':
                      description: Example response
            """.trimIndent()
        )

        val violations = rule.validateSchema(jsonNode)

        assertThat(violations).isEmpty()
    }
}
