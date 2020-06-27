package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.headers.Header
import io.swagger.v3.oas.models.parameters.Parameter

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "183",
    severity = Severity.MUST,
    title = "Use Only the Specified Proprietary Zalando Headers"
)
class ProprietaryHeadersRule(rulesConfig: Config) {
    private val zalandoHeaders = rulesConfig.getConfig(javaClass.simpleName).getStringList("zalando_headers")
    private val standardRequestHeaders =
        rulesConfig.getConfig(javaClass.simpleName).getStringList("standard_request_headers")
    private val standardResponseHeaders =
        rulesConfig.getConfig(javaClass.simpleName).getStringList("standard_response_headers")

    private val requestHeaders = (standardRequestHeaders + zalandoHeaders).map { it.toLowerCase() }
    private val responseHeaders = (standardResponseHeaders + zalandoHeaders).map { it.toLowerCase() }

    private val requestDescription = "use only standardized or specified request headers"
    private val responseDescription = "use only standardized or specified response headers"

    @Check(severity = Severity.MUST)
    fun validateRequestHeaders(context: Context): List<Violation> = requestHeaders(context)
        .filterNot { it.name.toLowerCase() in requestHeaders }
        .map { context.violation(requestDescription, it) }

    @Check(severity = Severity.MUST)
    fun validateResponseHeaders(context: Context): List<Violation> = responseHeaders(context)
        .filterNot { it.key.toLowerCase() in responseHeaders }
        .map { context.violation(responseDescription, it.value) }

    private fun requestHeaders(context: Context): List<Parameter> = context.api.paths?.values
        .orEmpty()
        .flatMap {
            it?.readOperations().orEmpty()
                .flatMap {
                    it.parameters.orEmpty()
                        .filter { "header" == it.`in` }
                }
        }

    private fun responseHeaders(context: Context): List<Map.Entry<String, Header>> = context.api.paths?.values
        .orEmpty()
        .flatMap {
            it?.readOperations().orEmpty()
                .flatMap {
                    it.responses.orEmpty().values
                        .flatMap { it.headers.orEmpty().entries }
                }
        }
}
