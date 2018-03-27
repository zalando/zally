package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.headers.Header

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "153",
        severity = Severity.MUST,
        title = "Use 429 With Header For Rate Limits"
)
class Use429HeaderForRateLimitRule {

    private val description = "If Client Exceed Request Rate, Response Code Must Contain Header Information Providing Further Details to Client"
    private val xRateLimitHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        val paths = adapter.openAPI.paths.orEmpty().flatMap { (path, pathObj) ->
            pathObj.readOperationsMap().orEmpty().entries.flatMap { (verb, operation) ->
                operation.responses.orEmpty().flatMap { (code, response) ->
                    if (code == "429" && !containsRateLimitHeader(response.headers.orEmpty()))
                        listOf("$path $verb $code")
                    else emptyList()
                }
            }
        }
        return if (paths.isNotEmpty())
            Violation(description, paths)
        else null
    }

    private fun containsRateLimitHeader(headers: Map<String, Header>): Boolean =
            headers.containsKey("Retry-After") || headers.keys.containsAll(xRateLimitHeaders)
}
