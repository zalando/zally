package de.zalando.zally.ruleset.sbb

import de.zalando.zally.core.plus
import de.zalando.zally.core.toEscapedJsonPointer
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#identify-resources-and-sub-resources-via-path-segments",
    severity = Severity.SHOULD,
    title = "Resources must be identified via path segments"
)
class IdentifyResourcesViaPathSegments {
    private val pathStartsWithParameter = "Path must start with a resource"
    private val pathContainsSuccessiveParameters = "Path must not contain successive parameters"
    private val pathParameterContainsPrefixOrSuffix = "Path parameter must not contain prefixes and suffixes"

    private val pathStartingWithAParameter = """(^/\{[^/]+\}|/)""".toRegex()
    @Check(severity = Severity.SHOULD)
    fun pathStartsWithResource(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathStartingWithAParameter.matches(it.key) },
        action = { context.violations(pathStartsWithParameter, "/paths".toJsonPointer() + it.key.toEscapedJsonPointer()) })

    private val pathContainingSuccessiveParameters = """.*\}/\{.*""".toRegex()
    @Check(severity = Severity.SHOULD)
    fun pathDoesNotContainSuccessiveParameters(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathContainingSuccessiveParameters.matches(it.key) },
        action = { context.violations(pathContainsSuccessiveParameters, "/paths".toJsonPointer() + it.key.toEscapedJsonPointer()) })

    private val pathContainingPrefixedOrSuffixedParameter = """.*/([^/]+\{[^/]+\}|\{[^/]+\}[^/]+).*""".toRegex()
    @Check(severity = Severity.SHOULD)
    fun pathParameterDoesNotContainPrefixAndSuffix(context: Context): List<Violation> = context.validatePaths(
        pathFilter = { pathContainingPrefixedOrSuffixedParameter.matches(it.key) },
        action = { context.violations(pathParameterContainsPrefixOrSuffix, "/paths".toJsonPointer() + it.key.toEscapedJsonPointer()) })
}
