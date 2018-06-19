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
    title = "Use Standard HTTP Status Codes"
)
class UseStandardHttpStatusCodes(@Autowired rulesConfig: Config) {

    private val allowed = rulesConfig
        .getConfig("${javaClass.simpleName}.allowed")
        .entrySet()
        .map { (key, config) ->
            @Suppress("UNCHECKED_CAST")
            key to config.unwrapped() as List<String>
        }
        .toMap()

    @Check(severity = Severity.MUST)
    fun allowOnlyStandardStatusCodes(context: Context): List<Violation> {
        return context.api.paths.orEmpty().flatMap { (_, pathItem) ->
            pathItem.readOperationsMap().orEmpty().flatMap { (method, operation) ->
                operation.responses.filterNot { (statusCode, _) ->
                    isAllowed(method, statusCode)
                }.map { (_, response) ->
                    response
                }
            }.map {
                context.violation("Operations should use standard HTTP status codes", it)
            }
        }
    }

    private fun isAllowed(method: PathItem.HttpMethod, statusCode: String): Boolean {
        val allowedMethods = allowed[statusCode.toLowerCase()].orEmpty()
        return allowedMethods.contains(method.name) || allowedMethods.contains("ALL")
    }
}
