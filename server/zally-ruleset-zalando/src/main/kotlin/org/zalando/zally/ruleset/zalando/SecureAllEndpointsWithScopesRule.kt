package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.zalando.zally.core.util.allFlows
import org.zalando.zally.core.util.allScopes
import org.zalando.zally.core.util.getAllSecuritySchemes
import org.zalando.zally.core.util.isOAuth2
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

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
            .filter { it.isOAuth2() }
            .filterNotNull()
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
        val securitySchemes = context.api.getAllSecuritySchemes()

        // val definedScopes = definedScopes(context.api)
        return context.validateOperations(pathFilter = this::pathFilter) { (_, op) ->
            op?.let {
                val definedOpSecurityRequirements = definedSecurityRequirements(op, context.api)

                if (definedOpSecurityRequirements.isEmpty()) {
                    context.violations("Endpoint is not secured by scope(s)", op.security ?: op)
                } else {
                    definedOpSecurityRequirements.flatMap {
                        it.map { (opSchemeName, opScopes) ->
                            val matchingScheme = securitySchemes[opSchemeName]
                            when {
                                matchingScheme == null -> {
                                    context.violation("Security scheme $opSchemeName not found", op)
                                }
                                matchingScheme.isOAuth2() -> {
                                    validateOAuth2Schema(context, op, opScopes, matchingScheme, opSchemeName)
                                }
                                else -> null
                            } // Scopes are only used with OAuth 2 and OpenID Connect
                        }
                    }
                }
            }.orEmpty()
        }
    }

    private fun definedSecurityRequirements(operation: Operation, api: OpenAPI): List<SecurityRequirement> {
        val operationSecurity = operation.security.orEmpty()
        if (operationSecurity.isEmpty()) {
            return api.security.orEmpty()
        }
        return operationSecurity
    }

    private fun validateOAuth2Schema(
        context: Context,
        op: Operation,
        requestedScopes: List<String?>,
        definedScheme: SecurityScheme,
        schemeName: String
    ): Violation? {
        if (requestedScopes.isEmpty()) {
            return context.violation(
                "Endpoint is not secured by OAuth2 scope(s)",
                op.security ?: op
            )
        }
        val definedScopes = definedScheme.allScopes()
        val undefined =
            requestedScopes.filterNotNull().filterNot { sc -> definedScopes.contains(sc) }
        return if (undefined.isNotEmpty()) {
            context.violation(
                "Endpoint is secured by undefined OAuth2 scope(s): $schemeName:${undefined.joinToString()}",
                op.security ?: op
            )
        } else {
            null
        }
    }

    private fun pathFilter(entry: Map.Entry<String, PathItem?>): Boolean =
        pathWhitelist.none { it.containsMatchIn(entry.key) }
}
