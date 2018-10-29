package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "224",
    severity = Severity.MUST,
    title = "Follow Naming Convention for Hostnames"
)
class FunctionalNamingForHostnamesRule {
    private val audienceExtension = "x-audience"

    private val description = "hostname has to follow the functional naming schema"

    private val mustFollow = listOf("external-public", "external-partner")
    private val shouldFollow = listOf("company-internal", "business-unit-internal")
    private val mayFollow = listOf("component-internal")

    private val functionalDomain = """[a-z][a-z0-9]*"""
    private val functionalComponent = """[a-z][a-z0-9-]*"""
    private val functionHostnameURLRegEx =
        """(https://)?$functionalDomain-$functionalComponent\.zalandoapis\.com.*""".toRegex()

    @Check(severity = Severity.MUST)
    fun mustFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(mustFollow)(context)

    @Check(severity = Severity.SHOULD)
    fun shouldFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(shouldFollow)(context)

    @Check(severity = Severity.MAY)
    fun mayFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(mayFollow)(context)

    internal fun isUrlValid(url: String): Boolean = functionHostnameURLRegEx.matches(url)

    private fun checkHostnames(audiencesToCheck: List<String>): (context: Context) -> List<Violation> = { context ->
        val audience = context.api.info?.extensions?.get(audienceExtension)
        val hostnames = context.api.servers.orEmpty()

        when {
            audience is String && audience in audiencesToCheck -> hostnames
                .asSequence()
                .filterNot { isUrlValid(it.url) }
                .map { context.violation(description, it.url) }
                .toList()
            else -> emptyList()
        }
    }
}
