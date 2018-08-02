package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.io.Resources
import de.zalando.zally.rule.JsonSchemaValidator
import de.zalando.zally.rule.ObjectTreeReader
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.ast.JsonPointers
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "176",
    severity = Severity.MUST,
    title = "Use Problem JSON"
)
class UseProblemJsonRule {
    private val problemDetailsObjectMediaType = "application/problem+json"
    private val description = "Operations should return problem JSON when any problem occurs during processing " +
        "whether caused by client or server."

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
                        context.violation(description, schema)
                            .let {
                                Violation(
                                    "${it.description} ${validation.description}",
                                    it.pointer?.append(validation.pointer ?: JsonPointers.empty())
                                        ?: JsonPointers.empty()
                                )
                            }
                    }
            }
        }
    }

    private fun testForProblemSchema(response: ApiResponse): List<Pair<Any, Violation>> =
        response.content?.flatMap { (type, mediaType) ->
            System.out.println("TYPE: $type")
            if (!type.startsWith(problemDetailsObjectMediaType)) {
                val message = Violation("Media type have to be 'application/problem+json'")
                return listOf(Pair(mediaType, message))
            }
            val node = objectMapper.convertValue(mediaType.schema, JsonNode::class.java)
            val result = problemSchemaValidator.validate(node)
            result.map { Pair(mediaType.schema, it) }
        } ?: emptyList()
}
