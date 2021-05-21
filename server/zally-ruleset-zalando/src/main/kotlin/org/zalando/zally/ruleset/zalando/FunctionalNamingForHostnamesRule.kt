package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.ruleset.zalando.model.ApiAudience
import org.zalando.zally.ruleset.zalando.model.UnsupportedAudienceException
import org.zalando.zally.ruleset.zalando.model.apiAudience

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "224",
    severity = Severity.MUST,
    title = "Follow Naming Convention for Hostnames"
)
class FunctionalNamingForHostnamesRule(rulesConfig: Config) {
    private val description = "hostname has to follow the functional naming schema"

    private val mustFollow = listOf(ApiAudience.EXTERNAL_PUBLIC, ApiAudience.EXTERNAL_PARTNER)
    private val shouldFollow = listOf(ApiAudience.COMPANY_INTERNAL, ApiAudience.BUSINESS_UNIT_INTERNAL)
    private val mayFollow = listOf(ApiAudience.COMPONENT_INTERNAL)

    private val functionalDomain = """[a-z][a-z0-9]*"""
    private val functionalComponent = """[a-z][a-z0-9-]*"""
    private val functionHostnameURLRegEx =
        """(https://)?$functionalDomain-$functionalComponent\.zalandoapis\.com.*""".toRegex()

    private val applicationId = """[a-z][a-z0-9]*"""
    private val organizationUnit = """[a-z][a-z0-9]*"""
    private val legacyHostnameURLRegEx =
        """(https://)?$applicationId\.$organizationUnit\.zalan\.do.*""".toRegex()

    @Suppress("UNCHECKED_CAST")
    private val audienceExceptions = rulesConfig.getConfig("${javaClass.simpleName}.audience_exceptions").entrySet()
        .map { (key, config) -> key to config.unwrapped() as List<String?> }.toMap()

    @Check(severity = Severity.MUST)
    fun mustFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, mustFollow)

    @Check(severity = Severity.SHOULD)
    fun shouldFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, shouldFollow)

    @Check(severity = Severity.MAY)
    fun mayFollowFunctionalNaming(context: Context): List<Violation> = checkHostnames(context, mayFollow)

    internal fun isUrlValid(url: String, apiAudience: ApiAudience): Boolean =
        functionHostnameURLRegEx.matches(url) ||
            isValidLegacyUrl(url, apiAudience) ||
            isUrlInExceptionList(url, apiAudience)

    private fun isValidLegacyUrl(url: String, apiAudience: ApiAudience): Boolean =
        apiAudience == ApiAudience.COMPONENT_INTERNAL && legacyHostnameURLRegEx.matches(url)

    private fun isUrlInExceptionList(url: String, apiAudience: ApiAudience): Boolean =
        apiAudience == ApiAudience.EXTERNAL_PARTNER && audienceExceptions[apiAudience.code]?.contains(url) ?: false

    private fun checkHostnames(context: Context, audiencesToCheck: List<ApiAudience>): List<Violation> {
        val apiAudience = try {
            context.api.info?.apiAudience()
        } catch (e: UnsupportedAudienceException) {
            return context.violations(e.message!!, context.api.info)
        }
        return when {
            apiAudience !in audiencesToCheck -> emptyList()
            apiAudience == null -> context.violations("API info section is not defined", context.api)
            context.swagger != null -> checkHostnamesInSwaggerHost(context, apiAudience)
            else -> checkHostnamesInOpenAPIServers(context, apiAudience)
        }
    }

    private fun checkHostnamesInOpenAPIServers(context: Context, apiAudience: ApiAudience): List<Violation> =
        context.api.servers
            .orEmpty()
            .asSequence()
            .filterNot { isUrlValid(it.url, apiAudience) }
            .map { context.violation(description, it.url) }
            .toList()

    private fun checkHostnamesInSwaggerHost(context: Context, apiAudience: ApiAudience): List<Violation> =
        context.swagger!!.host.let { host ->
            when {
                host == null || isUrlValid(host, apiAudience) -> emptyList()
                else -> context.violations(description, "/host".toJsonPointer())
            }
        }
}
