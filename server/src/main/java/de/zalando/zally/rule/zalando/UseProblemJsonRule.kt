package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.extensions.producesJson
import io.swagger.models.ComposedModel
import io.swagger.models.Model
import io.swagger.models.RefModel
import io.swagger.models.Swagger
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "176",
        severity = Severity.MUST,
        title = "Use Problem JSON"
)
class UseProblemJsonRule {
    private val description = "Operations Should Return Problem JSON When Any Problem Occurs During Processing " +
            "Whether Caused by Client Or Server"
    private val requiredFields = setOf("title", "status")

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        val openApi = adapter.openAPI
        val paths = adapter.openAPI.paths.orEmpty().flatMap { pathEntry ->
            pathEntry.value.readOperationsMap().orEmpty()
                    .filter { it.key.shouldContainPayload() }
                    .flatMap { opEntry ->
                        opEntry.value.responses.orEmpty().flatMap { responseEntry ->
                            val httpCode = responseEntry.key.toIntOrNull()
                            if (httpCode in 400..599 && !isValidProblemJson(openApi, responseEntry.value, opEntry.value)) {
                                listOf("${pathEntry.key} ${opEntry.key} ${responseEntry.key}")
                            } else emptyList()
                        }
                    }
        }

        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }

    private fun isValidProblemJson(openAPI: OpenAPI, response: ApiResponse, operation: Operation) =
            isProblemJson(openAPI, response) && operation.producesJson

    private fun isProblemJson(openAPI: OpenAPI, response: ApiResponse): Boolean {
/*        val schema = response.schema
        val properties = when (schema) {
            is RefProperty -> getProperties(openAPI, openAPI.definitions?.get((response.schema as RefProperty).simpleRef))
            is ObjectProperty -> schema.properties?.keys.orEmpty()
            else -> emptySet<String>()
        }
        return properties.containsAll(requiredFields)*/
        //TODO refactor it
        return false
    }

    private fun getProperties(swagger: Swagger, definition: Model?): Set<String> {
        return when (definition) {
            is ComposedModel -> definition.allOf.orEmpty().flatMap { getProperties(swagger, it) }.toSet()
            is RefModel -> getProperties(swagger, swagger.definitions[definition.simpleRef])
            else -> definition?.properties?.keys.orEmpty()
        }
    }

    private fun PathItem.HttpMethod.shouldContainPayload(): Boolean =
            name.toLowerCase() !in listOf("head", "options")
}
