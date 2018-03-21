package de.zalando.zally.rule.zalando

import com.google.common.collect.Sets
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.extensions.allFlows
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.security.SecurityScheme

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "104",
        severity = Severity.MUST,
        title = "Secure Endpoints with OAuth 2.0"
)
class SecureWithOAuth2Rule {
    private val description = "Every endpoint must be secured by OAuth2 properly"

    @Check(severity = Severity.MUST)
    fun checkSecurityDefinitions(adapter: ApiAdapter): Violation? {
        val hasOAuth = adapter.openAPI.components.securitySchemes.orEmpty()
                .values.any { it.type == SecurityScheme.Type.OAUTH2 }

        val containsHttpScheme = adapter.openAPI.servers.orEmpty()
                .any { it.url.startsWith(SecurityScheme.Type.HTTP.name) }

        return if (!hasOAuth) {
            Violation("No OAuth2 security definitions found", emptyList())
        } else if (containsHttpScheme) {
            Violation("OAuth2 should be only used together with https", emptyList())
        } else {
            null
        }
    }

    @Check(severity = Severity.SHOULD)
    fun checkPasswordFlow(adapter: ApiAdapter): Violation? {
        val definitionsWithoutPasswordFlow = adapter.openAPI.components
                .securitySchemes
                .orEmpty()
                .values
                .filter { it.type == SecurityScheme.Type.OAUTH2 }
                .filter { it?.flows?.clientCredentials != null }

        return if (definitionsWithoutPasswordFlow.any())
            Violation("OAuth2 security definitions should use application flow", emptyList())
        else null
    }

    @Check(severity = Severity.MUST)
    fun checkUsedScopes(adapter: ApiAdapter): Violation? {
        val swagger = adapter.openAPI
        val definedScopes = getDefinedScopes(swagger)
        val hasTopLevelScope = hasTopLevelScope(adapter, definedScopes)
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathKey, path) ->
            path.readOperationsMap().orEmpty().entries.map { (method, operation) ->
                val actualScopes = extractAppliedScopes(operation)
                val undefinedScopes = Sets.difference(actualScopes, definedScopes)
                val unsecured = undefinedScopes.size == actualScopes.size && !hasTopLevelScope
                val msg = when {
                    undefinedScopes.isNotEmpty() ->
                        "undefined scopes: " + undefinedScopes.map { "'${it.second}'" }.joinToString(", ")
                    unsecured ->
                        "no valid OAuth2 scope"
                    else -> null
                }
                if (msg != null) "$pathKey $method has $msg" else null
            }.filterNotNull()
        }
        return if (!paths.isEmpty()) {
            Violation(description, paths)
        } else null
    }

    // get the scopes from security definition
    private fun getDefinedScopes(openApi: OpenAPI): Set<Pair<String, String>> =
            openApi.components.securitySchemes.flatMap { (group, scheme) ->
                scheme.flows.allFlows()
                        .flatMap { flow -> flow.scopes.keys }
                        .map { scope -> group to scope }
            }.toSet()

    // Extract all oauth2 scopes applied to the given operation into a simple list
    private fun extractAppliedScopes(operation: Operation): Set<Pair<String, String>> =
            operation.security?.flatMap { groupDefinition ->
                groupDefinition.entries.flatMap { (group, scopes) ->
                    scopes.map { group to it }
                }
            }.orEmpty().toSet()

    private fun hasTopLevelScope(adapter: ApiAdapter, definedScopes: Set<Pair<String, String>>): Boolean {
/*
        adapter.openAPI.security?.any { securityRequirement ->
            securityRequirement.requirements.entries.any { (group, scopes) ->
                scopes.any { scope -> (group to scope) in definedScopes }
            }
        } ?: false
*/
        //TODO rewrite it
        return false
    }
}
