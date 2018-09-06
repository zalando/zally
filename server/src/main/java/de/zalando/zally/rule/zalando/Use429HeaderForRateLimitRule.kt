package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "153",
    severity = Severity.MUST,
    title = "Use 429 With Header For Rate Limits"
)
class Use429HeaderForRateLimitRule {

    private val description = "Response has to contain rate limit information via headers"
    private val xRateLimitHeaders = listOf("X-RateLimit-Limit", "X-RateLimit-Remaining", "X-RateLimit-Reset")

    @Check(severity = Severity.MUST)
    fun checkHeadersForRateLimiting(context: Context): List<Violation> =
        context.api.paths.orEmpty().values.flatMap { it.readOperations().flatMap { it.responses.orEmpty().entries } }
            .filter { (code, _) -> "429" == code }.map { it.value }
            .filterNot { containsRateLimitHeader(it.headers.orEmpty().keys) }
            .map { context.violation(description, it) }

    private fun containsRateLimitHeader(headers: Collection<String>): Boolean =
        "Retry-After" in headers || headers.containsAll(xRateLimitHeaders)
}
