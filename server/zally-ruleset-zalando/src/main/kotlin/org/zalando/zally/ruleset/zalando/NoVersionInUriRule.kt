package org.zalando.zally.ruleset.zalando

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.servers.Server
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import java.net.URI

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "115",
    severity = Severity.MUST,
    title = "Do Not Use URI Versioning"
)
class NoVersionInUriRule {
    private val description = "URL contains version number"
    private val versionRegex = "(^|[^A-Za-z0-9])v[0-9]+([^A-Za-z0-9]|$)".toRegex(RegexOption.IGNORE_CASE)

    @Check(severity = Severity.MUST)
    fun checkServerURLs(context: Context): List<Violation> =
        (violatingServers(context.api) + violatingPaths(context.api))
            .map { context.violation(description, it) }

    private fun violatingServers(api: OpenAPI): Collection<Server> =
        api.servers.orEmpty()
            .filterNotNull()
            .filter { server ->
                // only evaluate the PATH part of the URL, never host/scheme/query
                val path = runCatching { URI(server.url).path }.getOrNull().orEmpty()
                path.matches(versionRegex) || versionRegex.containsMatchIn(path)
            }

    private fun violatingPaths(api: OpenAPI): Collection<PathItem> =
        api.paths.orEmpty().entries
            .filter { (path, _) -> versionRegex.containsMatchIn(path) }
            .map { (_, pathEntry) -> pathEntry }
}
