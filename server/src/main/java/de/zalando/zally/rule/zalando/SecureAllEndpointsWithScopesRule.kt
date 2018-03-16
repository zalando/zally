package de.zalando.zally.rule.zalando

import com.google.common.collect.Sets
import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Operation
import io.swagger.models.Swagger
import io.swagger.models.auth.OAuth2Definition
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "105",
        severity = Severity.MUST,
        title = "Secure All Endpoints With Scopes"
)
class SecureAllEndpointsWithScopesRule(@Autowired rulesConfig: Config) {

    private val pseudoScopes = rulesConfig.getStringList("${SecureAllEndpointsWithScopesRule::class.java.simpleName}.psuedo-scopes")

    private val accessTypes = rulesConfig.getStringList("${SecureAllEndpointsWithScopesRule::class.java.simpleName}.access-types")

    @Check(severity = Severity.MUST)
    fun checkDefinedScopeFormats(swagger: Swagger): Violation? {
        return swagger.securityDefinitions.orEmpty().flatMap { (schemeKey, scheme) ->
            when (scheme) {
                is OAuth2Definition -> {
                    scheme.scopes.orEmpty().flatMap { (scope, description) ->
                        checkDefinedScopeFormat(scope)?.let {
                            listOf("securityDefinitions $schemeKey $scope: $it")
                        } ?: emptyList()
                    }
                }
                else -> emptyList()
            }
        }.takeIf { it.isNotEmpty() }?.let { Violation("Defined scopes should match an expected format", it) }
    }

    fun checkDefinedScopeFormat(scope: String): String? {
        val components = scope.split('.')
        return when {
            components.size == 1 ->
                if (components[0] in pseudoScopes) null
                else "pseudo-scope '${components[0]}' should be one of $pseudoScopes"
            components.size > 3 -> "scopes should have no more than 3 dot-separated components"
            components.last() !in accessTypes -> "access-type '${components.last()}' should be one of $accessTypes"
            else -> null
        }
    }

    @Check(severity = Severity.MUST)
    fun checkOperationsAreScoped(swagger: Swagger): Violation? {
        val definedScopes = getDefinedScopes(swagger)
        val hasTopLevelScope = hasTopLevelScope(swagger, definedScopes)
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathKey, path) ->
            path.operationMap.orEmpty().entries.map { (method, operation) ->
                val actualScopes = extractAppliedScopes(operation)
                val undefinedScopes = Sets.difference(actualScopes, definedScopes)
                val unsecured = undefinedScopes.size == actualScopes.size && !hasTopLevelScope
                val msg = when {
                    unsecured ->
                        "no valid OAuth2 scope"
                    else -> null
                }
                if (msg != null) "$pathKey $method has $msg" else null
            }.filterNotNull()
        }
        return if (!paths.isEmpty()) {
            Violation("Every endpoint must be secured by some scope(s)", paths)
        } else null
    }

    // get the scopes from security definition
    private fun getDefinedScopes(swagger: Swagger): Set<Pair<String, String>> =
        swagger.securityDefinitions.orEmpty().entries.flatMap { (group, def) ->
            (def as? OAuth2Definition)?.scopes.orEmpty().keys.map { scope -> group to scope }
        }.toSet()

    // Extract all oauth2 scopes applied to the given operation into a simple list
    private fun extractAppliedScopes(operation: Operation): Set<Pair<String, String>> =
        operation.security?.flatMap { groupDefinition ->
            groupDefinition.entries.flatMap { (group, scopes) ->
                scopes.map { group to it }
            }
        }.orEmpty().toSet()

    private fun hasTopLevelScope(swagger: Swagger, definedScopes: Set<Pair<String, String>>): Boolean =
        swagger.security?.any { securityRequirement ->
            securityRequirement.requirements.entries.any { (group, scopes) ->
                scopes.any { scope -> (group to scope) in definedScopes }
            }
        } ?: false
}
