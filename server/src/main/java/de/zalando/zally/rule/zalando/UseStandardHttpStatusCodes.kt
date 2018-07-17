package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.PathItem
import org.springframework.beans.factory.annotation.Autowired

/**
 * Validate that HTTP methods and statuses align as expected
 */
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

    /**
     * Validate that HTTP methods and statuses align as expected
     * @param context the context to validate
     * @return list of identified violations
     */
    @Check(severity = Severity.MUST)
    fun allowOnlyStandardStatusCodes(context: Context): List<Violation> =
        context.validateOperations { (method, operation) ->
            operation.responses.orEmpty().filterNot { (status, _) ->
                isAllowed(method, status)
            }.map { (_, response) ->
                context.violation("Operations should use standard HTTP status codes", response)
            }
        }

    private fun isAllowed(method: PathItem.HttpMethod, statusCode: String): Boolean {
        val allowedMethods = allowed[statusCode.toLowerCase()].orEmpty()
        return allowedMethods.contains(method.name) || allowedMethods.contains("ALL")
    }
}
