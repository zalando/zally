package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.io.IOException
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

    private val jsonSchemaValidator: JsonSchemaValidator

    init {
        jsonSchemaValidator = getSchemaValidator(rulesConfig.getConfig(javaClass.simpleName))
    }

    @Check(severity = Severity.MUST)
    fun validate(swagger: JsonNode): List<Violation> {
        return jsonSchemaValidator.validate(swagger).let { validationResult ->
            validationResult.messages.map { message ->
                Violation(message.message, listOf(message.path))
            }
        }
    }

    private fun getSchemaValidator(ruleConfig: Config): JsonSchemaValidator {
        val schemaUrlProperty = "swagger_schema_url"
        if (!ruleConfig.hasPath(schemaUrlProperty)) {
            return getSchemaValidatorFromResource()
        }

        val swaggerSchemaUrl = URL(ruleConfig.getString(schemaUrlProperty))
        try {
            val schema = ObjectMapper().readTree(swaggerSchemaUrl)
            return JsonSchemaValidator(schema)
        } catch (ex: IOException) {
            log.warn("Unable to load swagger schema using URL: '$swaggerSchemaUrl' ${ex.message}. Using schema from resources.")
            return getSchemaValidatorFromResource()
        }
    }

    private fun getSchemaValidatorFromResource(): JsonSchemaValidator {
        // The downloadSwaggerSchema gradle task can be used to download latest versions of schemas
        val referencedOnlineSchema = "http://json-schema.org/draft-04/schema"
        val localResource = Resources.getResource("schemas/json-schema.json").toString()

        val schemaUrl = Resources.getResource("schemas/swagger-schema.json")
        val schema = ObjectTreeReader().read(schemaUrl)
        return JsonSchemaValidator(schema, schemaRedirects = mapOf(referencedOnlineSchema to localResource))
    }
}
