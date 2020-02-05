package de.zalando.zally.ruleset.zalando

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.io.Resources
import com.typesafe.config.Config
import de.zalando.zally.core.EMPTY_JSON_POINTER
import de.zalando.zally.core.JsonSchemaValidator
import de.zalando.zally.core.ObjectTreeReader
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.ruleset.zalando.UseOpenApiRule.OpenApiVersion.OPENAPI3
import de.zalando.zally.ruleset.zalando.UseOpenApiRule.OpenApiVersion.SWAGGER
import org.slf4j.LoggerFactory
import java.net.URL

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "101",
    severity = Severity.MUST,
    title = "Provide API Specification using OpenAPI"
)
class UseOpenApiRule(rulesConfig: Config) {

    private enum class OpenApiVersion {
        SWAGGER, OPENAPI3;

        val resource: URL by lazy {
            Resources.getResource("schemas/${name.toLowerCase()}-schema.json")
        }
    }

    private val log = LoggerFactory.getLogger(UseOpenApiRule::class.java)

    private val jsonSchemaValidators = getSchemaValidators(rulesConfig.getConfig(javaClass.simpleName))

    @Check(severity = Severity.MUST)
    fun validateSchema(spec: JsonNode): List<Violation> {
        val version = when {
            spec.get("swagger") != null -> SWAGGER
            else -> OPENAPI3
        }

        return jsonSchemaValidators[version]
            ?.validate(spec)
            .orEmpty()
            .map {
            Violation("Does not match ${version.name.toLowerCase()} schema: ${it.description}", it.pointer)
            }
    }

    @Check(severity = Severity.MUST)
    fun checkIfTheFormatIsYAML(context: Context): Violation? {
        // at this point the api specification has been already parsed successfully
        // -> the source is either a valid YAML or JSON format
        // -> JSON must start with '{' and end with '}'
        val cleanedUpSource = context.source.trim()
        return if (cleanedUpSource.startsWith("{") && cleanedUpSource.endsWith("}")) {
            context.violation("must use YAML format", EMPTY_JSON_POINTER)
        } else {
            null
        }
    }

    private fun getSchemaValidators(ruleConfig: Config): Map<OpenApiVersion, JsonSchemaValidator> {
        return try {
            val swaggerSchemaLink = URL(ruleConfig.getString("schema_urls.${SWAGGER.name.toLowerCase()}"))
            val openApiSchemaLink = URL(ruleConfig.getString("schema_urls.${OPENAPI3.name.toLowerCase()}"))

            val swaggerSchema = ObjectMapper().readTree(swaggerSchemaLink)
            val openApiSchema = ObjectMapper().readTree(openApiSchemaLink)

            // to avoid resolving the `id` property of the schema by the validator
            (swaggerSchema as ObjectNode).remove("id")
            (openApiSchema as ObjectNode).remove("id")

            return mapOf(
                SWAGGER to JsonSchemaValidator(swaggerSchema),
                OPENAPI3 to JsonSchemaValidator(openApiSchema)
            )
        } catch (e: Exception) {
            log.warn("Unable to load swagger schemas: ${e.message}. Using default schemas instead.")
            getDefaultSchemaValidators()
        }
    }

    private fun getDefaultSchemaValidators(): Map<OpenApiVersion, JsonSchemaValidator> {
        // The downloadSwaggerSchema gradle task can be used to download latest versions of schemas
        val referencedOnlineSchema = "http://json-schema.org/draft-04/schema"
        val localResource = Resources.getResource("schemas/json-schema.json").toString()

        return OpenApiVersion
            .values()
            .map { version ->
                val schemaUrl = version.resource
                val schema = ObjectTreeReader().read(schemaUrl)
                version to JsonSchemaValidator(schema, schemaRedirects = mapOf(
                    referencedOnlineSchema to localResource,
                    "http://swagger.io/v2/schema.json" to SWAGGER.resource.toString(),
                    "http://openapis.org/v3/schema.json" to OPENAPI3.resource.toString())
                )
            }
            .toMap()
    }
}
