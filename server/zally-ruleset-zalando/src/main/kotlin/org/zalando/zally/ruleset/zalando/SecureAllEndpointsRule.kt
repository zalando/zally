package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.isBearer
import org.zalando.zally.core.util.isOAuth2
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.security.SecurityScheme

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "104",
    severity = Severity.MUST,
    title = "Secure Endpoints"
)
class SecureAllEndpointsRule {

    @Check(severity = Severity.MUST)
    fun checkHasValidSecuritySchemes(context: Context): Violation? {
        val valid = context.api.components?.securitySchemes?.values?.filter {
            it.isOAuth2() || it.isBearer()
        }.orEmpty()

        return if (valid.isEmpty()) context.violation(
            "API must be secured by OAuth2 or Bearer Authentication",
            "/components/securitySchemes".toJsonPointer()
        ) else null
    }

    @Check(severity = Severity.MUST)
    fun checkHasNoInvalidSecuritySchemes(context: Context): List<Violation> =
        context.api.components?.securitySchemes?.values?.filterNot {
            it.isOAuth2() || it.isBearer()
        }?.map {
            context.violation("API must be secured by OAuth2 or Bearer Authentication", it)
        }.orEmpty()

    @Check(severity = Severity.MUST)
    fun checkUsedScopesAreSpecified(context: Context): List<Violation> {
        if (!context.isOpenAPI3()) return emptyList()

        val specifiedScopes = context.api.components?.securitySchemes?.entries
            .orEmpty()
            .filter { (_, scheme) -> SecurityScheme.Type.OAUTH2 == scheme.type }
            .flatMap { (group, scheme) ->
                scheme.flows?.clientCredentials?.scopes.orEmpty().keys.map { scope -> group to scope }
            }
            .toSet()

        val usedScopes = context.api.paths?.values
            .orEmpty()
            .flatMap { it?.readOperations().orEmpty().flatMap { it.security.orEmpty() } }
            .flatMap { secReq ->
                secReq.keys.flatMap { group ->
                    secReq[group].orEmpty().map { scope -> group to scope }
                }
            }

        return usedScopes
            .filterNot { it in specifiedScopes }
            .map { (group, scope) ->
                context.violation(
                    "The scope '$group/$scope' is not specified in the clientCredentials flow of the " +
                            "OAuth2 security definition",
                    scope
                )
            }
    }
}
