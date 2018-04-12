package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.io.Resources
import com.typesafe.config.Config
import de.zalando.zally.rule.JsonSchemaValidator
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.net.URL

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "101",
    severity = Severity.MUST,
    title = "Provide API Specification using OpenAPI"
)
open class UseOpenApiRule(@Autowired rulesConfig: Config) {

    private val log = LoggerFactory.getLogger(UseOpenApiRule::class.java)

    open val description = "Given file is not OpenAPI 2.0 compliant."

    private val jsonSchemaValidators: List<JsonSchemaValidator>

    private val defaultSchemas = listOf(
        Pair("Swagger 2.0", "schemas/openapi-2-schema.json"),
        Pair("OpenAPI 3", "schemas/openapi-3-schema.json")
    )

    init {
        jsonSchemaValidators = getSchemaValidators(rulesConfig.getConfig(javaClass.simpleName))
    }

    @Check(severity = Severity.MUST)
    fun validate(swagger: JsonNode): List<Violation> {
        val violations = mutableListOf<Violation>()

        for (validator in jsonSchemaValidators) {
            val result = validator.validate(swagger)
            if (result.isSuccess) {
                return emptyList()
            }
            if (violations.isEmpty()) {
                violations += result.messages.map {
                    Violation("Does not match ${validator.name}: ${it.message}", "#" + it.path)
                }
            }
        }
        return violations
    }

    private fun getSchemaValidators(ruleConfig: Config): List<JsonSchemaValidator> {
        return try {
            ruleConfig.getStringList("swagger_schema_urls").map {
                val url = URL(it)
                val schema = ObjectMapper().readTree(url)
                (schema as ObjectNode).remove("id")
                JsonSchemaValidator(it, schema)
            }
        } catch (e: Exception) {
            log.warn("Unable to load swagger schemas: ${e.message}. Using default schemas instead.")
            getDefaultSchemaValidators()
        }
    }

    private fun getDefaultSchemaValidators(): List<JsonSchemaValidator> {
        // The downloadSwaggerSchema gradle task can be used to download latest versions of schemas
        val referencedOnlineSchema = "http://json-schema.org/draft-04/schema"
        val localResource = Resources.getResource("schemas/json-schema.json").toString()

        return defaultSchemas.map { (name, file) ->
            val schemaUrl = Resources.getResource(file)
            val schema = ObjectTreeReader().read(schemaUrl)
            JsonSchemaValidator(name, schema, schemaRedirects = mapOf(referencedOnlineSchema to localResource))
        }
    }
}
