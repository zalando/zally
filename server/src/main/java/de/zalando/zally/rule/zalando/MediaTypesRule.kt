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
    fun validate(adapter: ApiAdapter): Violation? =
        adapter.withVersion2 { swagger ->
            val paths = swagger.paths.orEmpty().entries.flatMap { (pathName, path) ->
                path.operationMap.orEmpty().entries.flatMap { (verb, operation) ->
                    val mediaTypes = ArrayList<String>() + operation.produces.orEmpty() + operation.consumes.orEmpty()
                    val violatingMediaTypes = mediaTypes.filter(this::isViolatingMediaType)
                    if (violatingMediaTypes.isNotEmpty()) listOf("$pathName $verb") else emptyList()
                }
            }
            if (paths.isNotEmpty()) Violation(DESCRIPTION, paths) else null
        }


    private fun isViolatingMediaType(mediaType: String) =
        !isApplicationJsonOrProblemJson(mediaType) && !isCustomMediaTypeWithVersioning(mediaType)
}
