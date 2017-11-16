package de.zalando.zally.rule.zalando

import com.google.common.collect.Sets
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.dto.ViolationType.MUST
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Operation
import io.swagger.models.Scheme
import io.swagger.models.Swagger
import io.swagger.models.auth.OAuth2Definition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefineOAuthScopesRule(@Autowired ruleSet: ZalandoRuleSet) : AbstractRule(ruleSet) {
    override val title = "Define and Assign Access Rights (Scopes)"
    override val url = "/#104"
    override val violationType = MUST
    override val code = "M004"
    override val guidelinesCode = "104"
    private val DESC = "Every endpoint must be secured by proper OAuth2 scope"

    @Check
    fun secureWithOAuth2(swagger: Swagger): Violation? {
        val hasOAuth = swagger.securityDefinitions.orEmpty().values.any { it.type?.toLowerCase() == "oauth2" }
        val containsHttpScheme = swagger.schemes.orEmpty().contains(Scheme.HTTP)
        return if (!hasOAuth)
            Violation(this, "Secure Endpoints with OAuth 2.0", "No OAuth2 security definitions found", violationType, url, emptyList())
        else if (containsHttpScheme)
            Violation(this, "Secure Endpoints with OAuth 2.0", "OAuth2 should be only used together with https", violationType, url, emptyList())
        else
            null
    }

    @Check
    fun usePasswordFlowWithOAuth2(swagger: Swagger): Violation? {
        val definitionsWithoutPasswordFlow = swagger
                .securityDefinitions
                .orEmpty()
                .values
                .filter { it.type?.toLowerCase() == "oauth2" }
                .filter { (it as OAuth2Definition).flow != "password" }

        return if (definitionsWithoutPasswordFlow.any())
            Violation(this, "Set Flow to Password When Using OAuth 2.0", "OAuth2 security definitions should use password flow", ViolationType.SHOULD, url, emptyList())
        else null
    }

    @Check
    fun defineOAuthScopes(swagger: Swagger): Violation? {
        val definedScopes = getDefinedScopes(swagger)
        val hasTopLevelScope = hasTopLevelScope(swagger, definedScopes)
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathKey, path) ->
            path.operationMap.orEmpty().entries.map { (method, operation) ->
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
            Violation(this, title, DESC, violationType, url, paths)
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
