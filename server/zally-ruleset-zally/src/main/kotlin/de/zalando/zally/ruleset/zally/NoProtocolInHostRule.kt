package de.zalando.zally.ruleset.zally

import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

/**
 * Validates that [Swagger.host] does not contain a url scheme as required by
 * [OpenAPI 2.0](https://github.com/OAI/OpenAPI-Specification/blob/master/versions/2.0.md#swagger-object).
 *
 * > The host (name or ip) serving the API. This MUST be the host only and does not include the scheme nor sub-paths.
 *
 * Note that this rule operates on a [Context] but specifically targets [Swagger.host] since the OpenAPI 3.0.0
 * equivalent [io.swagger.v3.oas.models.servers.Server.url] is a url rather than "host only".
 */
@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "M008",
    severity = Severity.MUST,
    title = "Host should not contain protocol"
)
class NoProtocolInHostRule {

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        val host = context.swagger?.host.orEmpty()
        return when {

            context.isOpenAPI3() -> emptyList()

            "://" in host -> listOf(
                Violation(
                    "'$host' contains protocol information which should be listed separately as schemes",
                    "/host".toJsonPointer()
                )
            )

            else -> emptyList()
        }
    }
}
