package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import io.swagger.models.properties.Property
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class Use429HeaderForRateLimitRule(@Autowired ruleSet: ZalandoRuleSet) : AbstractRule(ruleSet) {

    override val title = "Use 429 With Header For Rate Limits"
    override val id = "153"
    private val DESCRIPTION = "If Client Exceed Request Rate, Response Code Must Contain Header Information Providing Further Details to Client"
    private val X_RATE_LIMIT_TRIO = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

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
            Violation(DESCRIPTION, paths)
        else null
    }

    private fun containsRateLimitHeader(headers: Map<String, Property>): Boolean =
        headers.containsKey("Retry-After") || headers.keys.containsAll(X_RATE_LIMIT_TRIO)
}
