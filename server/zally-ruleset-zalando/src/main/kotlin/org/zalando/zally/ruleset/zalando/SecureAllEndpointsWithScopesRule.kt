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
import org.zalando.zally.core.util.isBearer
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

                val requirements = definedSecurityRequirements(op, context.api)

                if (requirements.isEmpty()) {
                    context.violations(
                        "Endpoint is not secured by scope(s)", op.security ?: op
                    )
                } else {
                    requirements.flatMap {
                        it.map { (schemaName, operationScopes) ->
                            securitySchemes[schemaName]?.let { schema ->
                                when {
                                    schema.isOAuth2() -> {
                                        validateOAuth2Schema(
                                            context,
                                            op,
                                            operationScopes,
                                            schema,
                                            schemaName
                                        )
                                    }
                                    schema.isBearer() -> validateBearerSchema(context, op, schemaName)
                                    else -> null
                                }
                            }
                        }
                    }
                }
            }.orEmpty()
        }
    }

    private fun definedSecurityRequirements(operation: Operation, api: OpenAPI): List<SecurityRequirement> =
        api.security.orEmpty() + operation.security.orEmpty()

    private fun validateOAuth2Schema(
        context: Context,
        op: Operation,
        requestedScopes: List<String?>,
        scheme: SecurityScheme,
        schemeName: String
    ): Violation? {
        if (requestedScopes.isEmpty()) {
            return context.violation(
                "Endpoint is not secured by OAuth2 scope(s)", op.security ?: op
            )
        }
        val definedScopes = scheme.allScopes()
        val undefined =
            requestedScopes.filterNotNull().filterNot { sc -> definedScopes.contains(sc) }
        return if (undefined.isNotEmpty()) {
            context.violation(
                "Endpoint is secured by undefined OAuth2 scope(s): $schemeName:${undefined.joinToString()}",
                op.security ?: op
            )
        } else null
    }

    private fun validateBearerSchema(context: Context, op: Operation, schemeName: String): Violation? {
        val requirement = op.security?.find { it[schemeName] != null }
        return if (requirement == null) {
            context.violation("Endpoint is not secured by scope(s)", op.security)
        } else null
    }

    private fun pathFilter(entry: Map.Entry<String, PathItem?>): Boolean =
        pathWhitelist.none { it.containsMatchIn(entry.key) }
}
