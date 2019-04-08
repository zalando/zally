package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

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

    private val APPLICATION_PROBLEM_JSON_PATTERN = "^application/((problem|json-patch|merge-patch)\\+)?json$".toRegex()
    private val CUSTOM_WITH_VERSIONING_PATTERN = "^\\w+/[-+.\\w]+;v(ersion)?=\\d+$".toRegex()
    private val description = "Custom media types should only be used for versioning"

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            val consumedMediaTypes = operation?.requestBody?.content?.entries.orEmpty().toList()
            val producedMediaTypes = operation?.responses?.values.orEmpty().toList()
                .flatMap { it.content?.entries.orEmpty() }
            (consumedMediaTypes + producedMediaTypes)
                .filter { isViolatingMediaType(it.key) }
                .map { context.violation(description, it.value) }
        }

    private fun isViolatingMediaType(mediaType: String) =
        !isApplicationJsonOrProblemJson(mediaType) && !isCustomMediaTypeWithVersioning(mediaType)

    fun isApplicationJsonOrProblemJson(mediaType: String): Boolean =
        mediaType.matches(APPLICATION_PROBLEM_JSON_PATTERN)

    fun isCustomMediaTypeWithVersioning(mediaType: String): Boolean =
        mediaType.matches(CUSTOM_WITH_VERSIONING_PATTERN)
}
