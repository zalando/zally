package de.zalando.zally.ruleset.zalando

import de.zalando.zally.core.toJsonPointer
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
    fun mustFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, mustFollow)

    @Check(severity = Severity.SHOULD)
    fun shouldFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, shouldFollow)

    @Check(severity = Severity.MAY)
    fun mayFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, mayFollow)

    internal fun isUrlValid(url: String): Boolean = functionHostnameURLRegEx.matches(url)

    private fun checkHostnames(context: Context, audiencesToCheck: List<String>): List<Violation> = when {
        context.api.info?.extensions?.get(audienceExtension) !in audiencesToCheck -> emptyList()
        context.swagger != null -> checkHostnamesInSwaggerHost(context)
        else -> checkHostnamesInOpenAPIServers(context)
    }

    private fun checkHostnamesInOpenAPIServers(context: Context): List<Violation> = context.api.servers
        .orEmpty()
        .asSequence()
        .filterNot { isUrlValid(it.url) }
        .map { context.violation(description, it.url) }
        .toList()

    private fun checkHostnamesInSwaggerHost(context: Context): List<Violation> = context.swagger!!.host.let { host ->
        when {
            host == null || isUrlValid(host) -> emptyList()
            else -> context.violations(description, "/host".toJsonPointer())
        }
    }
}
