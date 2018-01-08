package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import io.swagger.models.Swagger
import io.swagger.models.properties.Property

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "153",
        severity = Severity.MUST,
        title = "Use 429 With Header For Rate Limits"
)
class Use429HeaderForRateLimitRule : AbstractRule() {

    private val description = "If Client Exceed Request Rate, Response Code Must Contain Header Information Providing Further Details to Client"
    private val xRateLimitHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().flatMap { (path, pathObj) ->
            pathObj.operationMap.orEmpty().entries.flatMap { (verb, operation) ->
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

    private fun containsRateLimitHeader(headers: Map<String, Property>): Boolean =
        headers.containsKey("Retry-After") || headers.keys.containsAll(xRateLimitHeaders)
}
