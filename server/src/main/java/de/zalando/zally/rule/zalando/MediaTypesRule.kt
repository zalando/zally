package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.Context
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning
import io.swagger.models.Swagger

/**
 * @see "https://opensource.zalando.com/restful-api-guidelines/#172"
 */
@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "172",
    severity = Severity.SHOULD,
    title = "Prefer standard media type names"
)
class MediaTypesRule {

    private val description = "Custom media types should only be used for versioning"

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().entries.flatMap { (pathName, path) ->
            path.operationMap.orEmpty().entries.flatMap { (verb, operation) ->
                val mediaTypes = ArrayList<String>() + operation.produces.orEmpty() + operation.consumes.orEmpty()
                val violatingMediaTypes = mediaTypes.filter(this::isViolatingMediaType)
                if (violatingMediaTypes.isNotEmpty()) listOf("$pathName $verb") else emptyList()
            }
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.api.paths.orEmpty().values.flatMap { path ->
            path.readOperations().flatMap { operation ->
                val consumedMediaViolations = operation.requestBody.content.orEmpty()
                    .filter { (mediaType, _) -> this.isViolatingMediaType(mediaType) }
                    .map { context.violation(description, it) } // todo #714: Check if pointer still valid without the 2nd parameter
                val producedMediaViolation = operation.responses.orEmpty().values
                    .flatMap { response -> response.content.orEmpty().entries }
                    .filter { (mediaType, _) -> isViolatingMediaType(mediaType) }
                    .map { context.violation(description, it) } // todo #714: Check if pointer still valid without the 2nd parameter
                consumedMediaViolations + producedMediaViolation
            }
        }

    private fun isViolatingMediaType(mediaType: String) =
        !isApplicationJsonOrProblemJson(mediaType) && !isCustomMediaTypeWithVersioning(mediaType)
}
