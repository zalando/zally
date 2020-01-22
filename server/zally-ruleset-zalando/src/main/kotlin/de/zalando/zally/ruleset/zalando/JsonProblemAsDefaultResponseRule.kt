package de.zalando.zally.ruleset.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "151",
    severity = Severity.MUST,
    title = "Specify Success and Error Responses"
)
class JsonProblemAsDefaultResponseRule {
    private val validRefs = listOf(
        "https://zalando.github.io/problem/schema.yaml#/Problem",
        "https://opensource.zalando.com/problem/schema.yaml#/Problem"
    )
    private val validContentTypes = listOf("application/json", "application/problem+json")

    @Check(severity = Severity.MAY)
    fun checkContainsDefaultResponse(context: Context): List<Violation> = responsesPerOperation(context)
        .filterNot { "default" in it.second.keys }
        .map { context.violation("operation has to contain the default response", it.first) }

    @Check(severity = Severity.MAY)
    fun checkDefaultResponseIsProblemJson(context: Context): List<Violation> = responsesPerOperation(context)
        .filter { "default" in it.second.keys }
        .flatMap { it.second.getValue("default").content.orEmpty().entries }
        .filter { (contentType, _) -> contentType in validContentTypes }
        .filterNot { it.value?.schema?.`$ref` in validRefs || isProblemJsonSchema(it.value?.schema) }
        .map { context.violation("problem json has to be used as default response (${validRefs[0]})", it.value) }

    private fun responsesPerOperation(context: Context): Collection<Pair<Operation, Map<String, ApiResponse>>> =
        context.api.paths?.values
            .orEmpty()
            .flatMap {
                it?.readOperations().orEmpty()
                    .map { operation -> Pair(operation, operation.responses.orEmpty()) }
            }

    private fun isProblemJsonSchema(schema: Schema<*>?): Boolean {
        val props = schema?.properties.orEmpty()
        return props["type"]?.type == "string" && props["type"]?.format == "uri" &&
            props["title"]?.type == "string" &&
            props["status"]?.type == "integer" && props["status"]?.format == "int32" &&
            props["detail"]?.type == "string" &&
            props["instance"]?.type == "string" && props["instance"]?.format == "uri"
    }
}
