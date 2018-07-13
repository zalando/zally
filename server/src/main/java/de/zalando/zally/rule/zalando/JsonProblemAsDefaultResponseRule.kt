package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponse

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "151",
        severity = Severity.MUST,
        title = "Specify Success and Error Responses"
)
class JsonProblemAsDefaultResponseRule {
    private val validRefs = listOf("https://zalando.github.io/problem/schema.yaml#/Problem",
            "https://opensource.zalando.com/problem/schema.yaml#/Problem")

    @Check(severity = Severity.MAY)
    fun checkContainsDefaultResponse(context: Context): List<Violation> = responsesPerOperation(context)
            .filterNot { "default" in it.second.keys }
            .map { context.violation("operation has to contain the default response", it.first) }

    @Check(severity = Severity.MAY)
    fun checkDefaultResponseIsProblemJson(context: Context): List<Violation> = responsesPerOperation(context)
            .filter { "default" in it.second.keys }
            .filterNot { it.second["default"]!!.`$ref` in validRefs }
            .map { context.violation("problem json has to be used as default response (${validRefs[0]})", it.first) }

    private fun responsesPerOperation(context: Context): Collection<Pair<Operation, Map<String, ApiResponse>>> =
            context.api.paths.values
                    .flatMap {
                        it.readOperations().orEmpty()
                                .map { operation -> Pair(operation, operation.responses.orEmpty()) }
                    }
}
