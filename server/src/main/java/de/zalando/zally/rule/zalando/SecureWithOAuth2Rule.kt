package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.security.SecurityScheme

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "104",
    severity = Severity.MUST,
    title = "Secure Endpoints with OAuth 2.0"
)
class SecureWithOAuth2Rule {

    @Check(severity = Severity.MUST)
    fun checkSecuritySchemesOAuth2IsUsed(context: Context): Violation? {
        val oauth2IsUsed = context.api.components.securitySchemes.values.any { SecurityScheme.Type.OAUTH2 == it.type }

        return if (!oauth2IsUsed) context.violation("API has to be secured by OAuth2", JsonPointer.compile("/components/securitySchemes"))
        else null
    }

    @Check(severity = Severity.MUST)
    fun checkSecuritySchemesOnlyOAuth2IsUsed(context: Context): List<Violation> =
        context.api.components?.securitySchemes?.values
            ?.filter { it.type != SecurityScheme.Type.OAUTH2 }
            ?.map {
                context.violation("Only OAuth2 is allowed to secure the API", it)
            }
            .orEmpty()

    @Check(severity = Severity.MUST)
    fun checkUsedScopesAreSpecified(context: Context): List<Violation> {
        if (!context.isOpenAPI3()) return emptyList()

        val specifiedScopes = context.api.components.securitySchemes.entries
            .filter { (_, scheme) -> SecurityScheme.Type.OAUTH2 == scheme.type }
            .flatMap { (group, scheme) ->
                scheme.flows?.clientCredentials?.scopes.orEmpty().keys.map { scope -> group to scope }
            }.toSet()

        val usedScopes = context.api.paths.values
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
                        "OAuth2 security definition", scope
                )
            }
    }
}
