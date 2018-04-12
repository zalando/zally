package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.PathItem
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "150",
    severity = Severity.MUST,
    title = "Use Specific HTTP Status Codes"
)
class UseSpecificHttpStatusCodes(@Autowired rulesConfig: Config) {

    private val allowedStatusCodes = rulesConfig
        .getConfig("${javaClass.simpleName}.allowed_codes")
        .entrySet()
        .map { (key, config) -> (key to config.unwrapped() as List<String>) }.toMap()

    @Check(severity = Severity.MUST)
    fun allowOnlySpecificStatusCodes(context: Context): List<Violation> {
        return context.api.paths.orEmpty().flatMap { (_, pathItem) ->
            pathItem.readOperationsMap().orEmpty().flatMap { (method, operation) ->
                operation.responses.filterNot { (statusCode, _) ->
                    isAllowed(method, statusCode)
                }.map { (_, response) ->
                    response
                }
            }.map {
                val pointer = context.pointerForValue(it) ?: context.currentPointer
                Violation("Operations should use specific HTTP status codes", pointer)
            }
        }
    }

    private fun isAllowed(method: PathItem.HttpMethod, statusCode: String) =
        allowedStatusCodes[method.name.toLowerCase()].orEmpty().contains(statusCode) ||
            allowedStatusCodes["all"].orEmpty().contains(statusCode)
}
