package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.io.Resources
import com.typesafe.config.Config
import de.zalando.zally.rule.JsonSchemaValidator
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.OpenApiVersion
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import org.slf4j.LoggerFactory
import java.net.URL

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "101",
    severity = Severity.MUST,
    title = "Provide API Specification using OpenAPI"
)
class UseOpenApiRule(rulesConfig: Config) {

    private val log = LoggerFactory.getLogger(UseOpenApiRule::class.java)

    val description = "Given file is not OpenAPI 2.0 compliant."

    private val jsonSchemaValidators: Map<OpenApiVersion, JsonSchemaValidator>

    private val defaultSchemas = mapOf(
        OpenApiVersion.SWAGGER to "schemas/openapi-2-schema.json",
        OpenApiVersion.OPENAPI3 to "schemas/openapi-3-schema.json"
    )

    init {
        jsonSchemaValidators = getSchemaValidators(rulesConfig.getConfig(javaClass.simpleName))
    }

    @Check(severity = Severity.MUST)
    fun validateSchema(spec: JsonNode): List<Violation> {
        val openApi3Spec = spec.get("swagger") == null
        val currentVersion = if (openApi3Spec) OpenApiVersion.OPENAPI3.version else OpenApiVersion.SWAGGER.version

        val swaggerValidator = jsonSchemaValidators[OpenApiVersion.SWAGGER]
        val openApi3Validator = jsonSchemaValidators[OpenApiVersion.OPENAPI3]

        return when {
            openApi3Spec -> openApi3Validator?.validate(spec).orEmpty()
            else -> swaggerValidator?.validate(spec).orEmpty()
        }.map {
            Violation("Does not match $currentVersion schema: ${it.description}", it.pointer)
        }
    }

    @Check(severity = Severity.MUST)
    fun checkIfTheFormatIsYAML(context: Context): Violation? {
        // at this point the api specification has been already parsed successfully
        // -> the source is either a valid YAML or JSON format
        // -> JSON must start with '{' and end with '}'
        val cleanedUpSource = context.source.trim()
        return if (cleanedUpSource.startsWith("{") && cleanedUpSource.endsWith("}")) {
            context.violation("must use YAML format", JsonPointers.EMPTY)
        } else {
            null
        }
    }

    private fun getSchemaValidators(ruleConfig: Config): Map<OpenApiVersion, JsonSchemaValidator> {
        return try {
            val swaggerSchemaLink = URL(ruleConfig.getString("schema_urls.${OpenApiVersion.SWAGGER.version}"))
            val openApiSchemaLink = URL(ruleConfig.getString("schema_urls.${OpenApiVersion.OPENAPI3.version}"))

            val swaggerSchema = ObjectMapper().readTree(swaggerSchemaLink)
            val openApiSchema = ObjectMapper().readTree(openApiSchemaLink)

            // to avoid resolving the `id` property of the schema by the validator
            (swaggerSchema as ObjectNode).remove("id")
            (openApiSchema as ObjectNode).remove("id")

            return mapOf(
                OpenApiVersion.SWAGGER to JsonSchemaValidator(OpenApiVersion.SWAGGER.version, swaggerSchema),
                OpenApiVersion.OPENAPI3 to JsonSchemaValidator(OpenApiVersion.OPENAPI3.version, openApiSchema)
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

        return defaultSchemas.map { (name, file) ->
            val schemaUrl = Resources.getResource(file)
            val schema = ObjectTreeReader().read(schemaUrl)
            JsonSchemaValidator(name.version, schema, schemaRedirects = mapOf(referencedOnlineSchema to localResource))
        }.associateBy { OpenApiVersion.valueOf(it.name.toUpperCase()) }
    }
}
