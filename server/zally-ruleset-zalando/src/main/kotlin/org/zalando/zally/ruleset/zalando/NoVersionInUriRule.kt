package org.zalando.zally.ruleset.zalando

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.servers.Server

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "115",
    severity = Severity.MUST,
    title = "Do Not Use URI Versioning"
)
class NoVersionInUriRule {
    private val description = "URL contains version number"
    private val versionRegex = "(.*)v[0-9]+(.*)".toRegex()

    @Check(severity = Severity.MUST)
    fun checkServerURLs(context: Context): List<Violation> =
        (violatingServers(context.api) + violatingPaths(context.api))
            .map { context.violation(description, it) }

    private fun violatingServers(api: OpenAPI): Collection<Server> =
        api.servers.orEmpty()
            .filter { it?.url?.matches(versionRegex) ?: false }

    private fun violatingPaths(api: OpenAPI): Collection<PathItem> =
        api.paths.orEmpty().entries
            .filter { (path, _) -> path.matches(versionRegex) }
            .map { (_, pathEntry) -> pathEntry }
}
