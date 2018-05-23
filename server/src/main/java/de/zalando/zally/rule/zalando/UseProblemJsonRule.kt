package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.JsonSchemaValidator
import de.zalando.zally.rule.JsonSchemaValidator.ValidationMessage
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "176",
    severity = Severity.MUST,
    title = "Use Problem JSON"
)
class UseProblemJsonRule {
    private val description = "Operations Should Return Problem JSON When Any Problem Occurs During Processing " +
        "Whether Caused by Client Or Server."

    private val objectMapper by lazy { ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL) }

    private val problemSchemaValidator by lazy {
        val schemaUrl = Resources.getResource("schemas/problem-meta-schema.json")
        val json = ObjectTreeReader().read(schemaUrl)
        JsonSchemaValidator("Problem", json)
    }

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        return context.api.paths.orEmpty().flatMap { (_, pathItem) ->
            pathItem.readOperations().flatMap {
                it.responses.orEmpty()
                    .filter { (code, _) ->
                        code.toIntOrNull() in 400..599 || code == "default"
                    }
                    .flatMap { (_, response) ->
                        testForProblemSchema(response)
                    }
                    .map { (schema, validation) ->
                        val pointer = (context.pointerForValue(schema) ?: "#") + validation.path
                        Violation("$description ${validation.message}", pointer)
                    }
            }
        }
    }

    private fun testForProblemSchema(response: ApiResponse): List<Pair<Schema<*>, ValidationMessage>> =
        response.content?.flatMap { (type, mediaType) ->
            if (!type.startsWith("application/json")) {
                val message = ValidationMessage("Media type must be application/json.", "")
                return listOf(Pair(mediaType.schema, message))
            }
            val node = objectMapper.convertValue(mediaType.schema, JsonNode::class.java)
            val result = problemSchemaValidator.validate(node)
            result.messages.map { Pair(mediaType.schema, it) }
        } ?: emptyList()
}
