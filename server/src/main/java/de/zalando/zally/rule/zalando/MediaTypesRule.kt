package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.media.MediaType

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

    private val standardMediaTypes = listOf(
        "application/json",
        "application/problem+json",
        "application/json-patch+json",
        "application/merge-patch+json"
    )

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            operation.mediaTypes()
                .filterNot { (type, _) ->
                    isStandardJsonMediaType(type)
                }
                .filterNot { (type, _) ->
                    isVersionedMediaType(type)
                }
                .map { (_, value) ->
                    context.violation(description, value)
                }
        }

    private fun Operation?.mediaTypes(): List<Pair<String, MediaType>> = mediaTypesConsumed() + mediaTypesProduced()

    private fun Operation?.mediaTypesConsumed() =
        this?.requestBody?.content?.map { (name, type) ->
            name to type
        }.orEmpty()

    private fun Operation?.mediaTypesProduced() =
        this?.responses?.values?.flatMap {
            it.content?.map { (name, type) ->
                name to type
            }.orEmpty()
        }.orEmpty()

    fun isStandardJsonMediaType(mediaType: String): Boolean = mediaType in standardMediaTypes

    fun isVersionedMediaType(mediaType: String): Boolean = "^\\w+/[-+.\\w]+;(v|version)=\\d+$".toRegex().matches(mediaType)
}
