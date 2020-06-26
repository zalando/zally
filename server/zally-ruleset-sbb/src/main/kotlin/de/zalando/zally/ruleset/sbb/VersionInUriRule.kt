package de.zalando.zally.ruleset.sbb

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.servers.Server

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#follow-versioning-best-practices",
    severity = Severity.MUST,
    title = "Do Use URI Versioning"
)
class VersionInUriRule {
    private val versionInServerRegex = "(.*)v[0-9]+[.](.*)".toRegex()
    private val versionInPathRegex = "(.*)\\/v[0-9]+\\/(.*)".toRegex()
    private val versionInResourceNameRegex = "(.*)\\/[^\\/]+v[0-9]+\\/(.*)".toRegex()

    @Check(severity = Severity.MUST)
    fun checkServerURLs(context: Context): List<Violation> =
        (violatingServers(context.api))
            .map { context.violation("Version found in host Name, but must be in Path.", it) }

    @Check(severity = Severity.MAY)
    fun checkPathUrls(context: Context): List<Violation> =
        (violatingPaths(context.api))
            .map { context.violation("No Version found in Path, consider using the major version at the beginning of the path.", it) }

    @Check(severity = Severity.MUST)
    fun checkResourceNames(context: Context): List<Violation> =
        (violatingResourceNames(context.api))
            .map { context.violation("Version found in resource name. Use versions only at the beginning of the path as an own resource, e.g. like .../v1/... .", it) }

    private fun violatingServers(api: OpenAPI): Collection<Server> =
        api.servers.orEmpty()
            .filter { it?.url?.matches(versionInServerRegex) ?: false }

    private fun violatingPaths(api: OpenAPI): Collection<PathItem> =
        api.paths.orEmpty().entries
            .filter { (path, _) -> !path.matches(versionInPathRegex) }
            .map { (_, pathEntry) -> pathEntry }

    private fun violatingResourceNames(api: OpenAPI): Collection<PathItem> =
        api.paths.orEmpty().entries
            .filter { (path, _) -> path.matches(versionInResourceNameRegex) }
            .map { (_, pathEntry) -> pathEntry }
}
