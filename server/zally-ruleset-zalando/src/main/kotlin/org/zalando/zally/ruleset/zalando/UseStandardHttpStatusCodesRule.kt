package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import io.swagger.v3.oas.models.PathItem
import org.zalando.zally.core.util.HttpStatus
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

/**
 * Validate that HTTP methods and statuses align as expected
 */
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "150",
    severity = Severity.MUST,
    title = "Use Standard HTTP Status Codes"
)
class UseStandardHttpStatusCodesRule(rulesConfig: Config) {

    private val wellUnderstoodResponseCodesAndVerbs = rulesConfig
        .getConfig("${javaClass.simpleName}.well_understood")
        .entrySet()
        .map { (key, config) ->
            @Suppress("UNCHECKED_CAST")
            key to config.unwrapped() as List<String>
        }
        .toMap()

    private val wellUnderstoodResponseCode = wellUnderstoodResponseCodesAndVerbs.keys

    private val standardResponseCodes = rulesConfig.getStringList("${javaClass.simpleName}.standard")

    companion object Messages {
        fun wellUnderstoodViolationMessage(path: String, status: String, method: String) =
            "Path $path should not use $status status code for $method operation"

        fun noContentViolationMessage(statusCode: String) =
            "$statusCode response should have no payload defined"
    }

    /**
     * Validate that well-understood HTTP response codes are used properly
     * @param context the context to validate
     * @return list of identified violations
     */
    @Check(severity = Severity.SHOULD)
    fun checkWellUnderstoodResponseCodesUsage(context: Context): List<Violation> =
        context.validatePaths { (pathName, pathItem) ->
            pathItem?.readOperationsMap().orEmpty().flatMap { (method, operation) ->
                operation?.responses.orEmpty()
                    .filterKeys { status -> !isAllowed(method, status) }
                    .map { (status, response) ->
                        context.violation(wellUnderstoodViolationMessage(pathName, status, method.name), response)
                    }
            }
        }

    /**
     * Validate that only standardized HTTP response codes are used
     * @param context the context to validate
     * @return list of identified violations
     */
    @Check(severity = Severity.MUST)
    fun checkIfOnlyStandardizedResponseCodesAreUsed(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            operation?.responses.orEmpty().filterKeys { status ->
                status !in standardResponseCodes
            }.map { (status, response) ->
                context.violation("$status is not a standardized response code", response)
            }
        }

    /**
     * Check that well-understood HTTP response codes are used
     * @param context the context to validate
     * @return list of identified violations
     */
    @Check(severity = Severity.SHOULD)
    fun checkIfOnlyWellUnderstoodResponseCodesAreUsed(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            operation?.responses.orEmpty().filterKeys { status ->
                status !in wellUnderstoodResponseCode
            }.map { (status, response) ->
                context.violation("$status is not a well-understood response code", response)
            }
        }

    @Check(severity = Severity.SHOULD)
    fun checkThatNoContentResponseHasNoContentDefined(context: Context): List<Violation> =
        context.validateResponses(
            operationFilter = { (method, _) ->
                method in listOf(
                    PathItem.HttpMethod.PUT,
                    PathItem.HttpMethod.PATCH,
                    PathItem.HttpMethod.DELETE
                )
            },
            responseFilter = { (code, response) ->
                code == HttpStatus.NoContent.code.toString() && response?.content.orEmpty().isNotEmpty()
            },
            action = { (key, response) ->
                if (response == null) {
                    emptyList<Violation>()
                } else {
                    listOf(context.violation(noContentViolationMessage(key), response))
                }
            }
        )

    private fun isAllowed(method: PathItem.HttpMethod, statusCode: String): Boolean {
        val allowedMethods = wellUnderstoodResponseCodesAndVerbs[statusCode.lowercase()].orEmpty()
        return allowedMethods.contains(method.name) || allowedMethods.contains("ALL")
    }
}
