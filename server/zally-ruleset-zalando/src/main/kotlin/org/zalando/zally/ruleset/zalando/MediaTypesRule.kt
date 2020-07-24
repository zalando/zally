package org.zalando.zally.ruleset.zalando

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse

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

    private val versionedMediaType = "^\\w+/[-+.\\w]+;(v|version)=\\d+$".toRegex()

    private val description = "Custom media types should only be used for versioning"

    private val standardMediaTypes = listOf(
        "application/json",
        "application/problem+json",
        "application/json-patch+json",
        "application/merge-patch+json"
    )

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> =
        context.api.mediaTypes()
            .filterNot { (type, _) -> isStandardJsonMediaType(type) }
            .filterNot { (type, _) -> isVersionedMediaType(type) }
            .map { (_, value) -> context.violation(description, value) }

    fun isStandardJsonMediaType(mediaType: String): Boolean = mediaType in standardMediaTypes

    fun isVersionedMediaType(mediaType: String): Boolean {
        return versionedMediaType.matches(mediaType)
    }

    private fun OpenAPI.mediaTypes(): List<Pair<String, MediaType>> =
        requestBodies().map { it.content }.flatMap { it.mediaTypes() } +
            apiResponses().map { it.content }.flatMap { it.mediaTypes() }

    private fun OpenAPI.requestBodies(): List<RequestBody> = components?.requestBodies?.values.orEmpty() +
        paths?.values?.flatMap { path ->
            path?.readOperations()?.mapNotNull { op ->
                op?.requestBody
            }.orEmpty()
        }.orEmpty()

    private fun OpenAPI.apiResponses(): Collection<ApiResponse> = components?.responses?.values.orEmpty() +
        paths?.values?.flatMap { path ->
            path?.readOperations()?.flatMap { op ->
                op?.responses?.values.orEmpty()
            }.orEmpty()
        }.orEmpty()

    private fun Content?.mediaTypes() = this
        ?.map { it.key to it.value }
        .orEmpty()
}
