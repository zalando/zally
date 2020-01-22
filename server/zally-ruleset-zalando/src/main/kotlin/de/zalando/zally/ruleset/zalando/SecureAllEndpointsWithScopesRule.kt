package de.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.security.SecurityScheme
import java.util.SortedSet

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "105",
    severity = Severity.MUST,
    title = "Secure All Endpoints With Scopes"
)
class SecureAllEndpointsWithScopesRule(rulesConfig: Config) {

    private val scopeRegex = Regex(
        rulesConfig.getString(
            "${SecureAllEndpointsWithScopesRule::class.java.simpleName}.scope_regex"
        )
    )

    private val pathWhitelist = rulesConfig.getStringList(
        "${SecureAllEndpointsWithScopesRule::class.java.simpleName}.path_whitelist"
    )
        .map { it.toRegex() }

    @Check(severity = Severity.MUST)
    fun checkDefinedScopeFormats(context: Context): List<Violation> =
        context.api.components?.securitySchemes?.values.orEmpty()
            .filter { it.type == SecurityScheme.Type.OAUTH2 }
            .flatMap { it.allFlows() }
            .flatMap { flow ->
                flow.scopes.orEmpty().keys.filterNot { scope ->
                    scopeRegex.matches(scope)
                }
                    .map { scope ->
                        context.violation("scope '$scope' does not match regex '$scopeRegex'", flow.scopes)
                    }
            }

    @Check(severity = Severity.MUST)
    fun checkOperationsAreScoped(context: Context): List<Violation> {
        val defined = defined(context.api)
        return context.validateOperations(pathFilter = this::pathFilter) { (_, op) ->
            op?.let {
                val requested = requested(context.api, op, defined)
                val undefined = undefined(requested, defined)
                when {
                    requested.isEmpty() -> context.violations("Endpoint not secured by OAuth2 scope(s)", op.security
                        ?: op)
                    undefined.isNotEmpty() -> context.violations(
                        "Endpoint secured by undefined OAuth2 scope(s): ${undefined.joinToString()}", op.security
                        ?: op
                    )
                    else -> emptyList()
                }
            }.orEmpty()
        }
    }

    private fun pathFilter(entry: Map.Entry<String, PathItem?>): Boolean =
        pathWhitelist.none { it.containsMatchIn(entry.key) }

    private fun SecurityScheme?.allFlows() = listOfNotNull(
        this?.flows?.implicit,
        this?.flows?.password,
        this?.flows?.clientCredentials,
        this?.flows?.authorizationCode
    )

    private fun defined(api: OpenAPI): Map<String, Set<String>> = api.components?.securitySchemes.orEmpty()
        .filterValues { scheme -> scheme.type == SecurityScheme.Type.OAUTH2 }
        .mapValues { it.value.allFlows().flatMap { it.scopes?.keys.orEmpty() }.toSet() }

    private fun requested(
        api: OpenAPI,
        op: io.swagger.v3.oas.models.Operation?,
        defined: Map<String, Set<String>>
    ): List<Pair<String, String>> = (op?.security ?: api.security ?: emptyList())
        .flatMap { requirement ->
            requirement
                .filterKeys { name -> defined.containsKey(name) }
                .flatMap { (name, scopes) -> scopes.map { name to it } }
        }

    private fun undefined(
        requested: List<Pair<String, String>>,
        defined: Map<String, Set<String>>
    ): SortedSet<String> = requested
        .filterNot { (name, scope) ->
            defined[name].orEmpty().contains(scope)
        }
        .map { "${it.first}:${it.second}" }
        .toSortedSet()
}
