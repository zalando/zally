package org.zalando.zally.ruleset.zalando

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.google.common.io.Resources
import com.typesafe.config.Config
import org.zalando.zally.core.EMPTY_JSON_POINTER
import org.zalando.zally.core.JsonSchemaValidator
import org.zalando.zally.core.ObjectTreeReader
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.ruleset.zalando.UseOpenApiRule.OpenApiVersion.OPENAPI3
import org.zalando.zally.ruleset.zalando.UseOpenApiRule.OpenApiVersion.SWAGGER
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

    private fun getSchemaValidators(config: Config): Map<OpenApiVersion, JsonSchemaValidator> {
        val defaultSchemaRedirects = mapOf(
            "http://json-schema.org/draft-04/schema" to Resources.getResource("schemas/json-schema.json"),
            "http://swagger.io/v2/schema.json" to SWAGGER.resource,
            "http://openapis.org/v3/schema.json" to OPENAPI3.resource,
            "https://spec.openapis.org/oas/3.0/schema/2019-04-02" to OPENAPI3.resource)
            .mapValues { (_, url) -> url.toString() }

        val reader = ObjectTreeReader()
        return OpenApiVersion
            .values()
            .map { version ->
                val configPath = "schema_urls.${version.name.toLowerCase()}"

                val (url, schemaRedirects) = when {
                    config.hasPath(configPath) -> URL(config.getString(configPath)) to emptyMap()
                    else -> version.resource to defaultSchemaRedirects
                }

                try {
                    val schema = reader.read(url)
                        .apply {
                            // to avoid resolving the `id` property of the schema by the validator
                            this as ObjectNode
                            remove("id")
                        }

                    version to JsonSchemaValidator(schema, schemaRedirects)
                } catch (e: Exception) {
                    log.error("Unable to load schema: $url", e)
                    throw e
                }
            }
            .toMap()
    }
}
