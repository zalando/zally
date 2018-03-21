package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isApplicationJsonOrProblemJson
import de.zalando.zally.util.PatternUtil.isCustomMediaTypeWithVersioning

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "172",
        severity = Severity.SHOULD,
        title = "Prefer standard media type names"
)
class MediaTypesRule {

    private val DESCRIPTION = "Custom media types should only be used for versioning"

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? {
        val paths = adapter.openAPI.paths.orEmpty().entries.flatMap { (pathName, path) ->
            path.readOperationsMap().orEmpty().entries.flatMap { (verb, operation) ->
                val requestMediaTypes = operation.requestBody?.content
                        .orEmpty()
                        .keys
                        .filterNotNull()
                val responseMediaTypes = operation.responses
                        .orEmpty()
                        .flatMap { (_, item) -> item.content.orEmpty().keys.filterNotNull() }

                val mediaTypes = requestMediaTypes + responseMediaTypes
                val violatingMediaTypes = mediaTypes.filter(this::isViolatingMediaType)
                if (violatingMediaTypes.isNotEmpty()) listOf("$pathName $verb") else emptyList()
            }
        }
        return if (paths.isNotEmpty()) Violation(DESCRIPTION, paths) else null
    }

    private fun isViolatingMediaType(mediaType: String) =
            !isApplicationJsonOrProblemJson(mediaType) && !isCustomMediaTypeWithVersioning(mediaType)
}
