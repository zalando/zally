package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "151",
        severity = Severity.HINT,
        title = "Not Specify Standard Error Codes"
)
class NotSpecifyStandardErrorCodesRule(@Autowired rulesConfig: Config) {
    private val description = "Not Specify Standard Error Status Codes Like 400, 404, 503 " +
            "Unless They Have Another Meaning Or Special Implementation/Contract Detail"

    private val standardErrorStatusCodes = rulesConfig.getConfig(javaClass.simpleName)
            .getIntList("standard_error_codes").toSet()

    @Check(severity = Severity.HINT)
    fun validate(swagger: Swagger): Violation? {

        val paths = swagger.paths.orEmpty().flatMap { pathEntry ->
            pathEntry.value.operationMap.orEmpty().flatMap { opEntry ->
                opEntry.value.responses.orEmpty().flatMap { responseEntry ->
                    val httpCode = responseEntry.key.toIntOrNull()
                    if (isStandardErrorCode(httpCode)) {
                        listOf("${pathEntry.key} ${opEntry.key} ${responseEntry.key}")
                    } else emptyList()
                }
            }
        }

        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }

    private fun isStandardErrorCode(httpStatusCode: Int?): Boolean {
        return httpStatusCode in standardErrorStatusCodes
    }
}
