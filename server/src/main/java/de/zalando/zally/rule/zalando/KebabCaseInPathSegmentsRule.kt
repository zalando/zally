package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "129",
    severity = Severity.MUST,
    title = "Lowercase words with hyphens"
)
class KebabCaseInPathSegmentsRule {

    private val description = "Use lowercase separate words with hyphens for path segments"

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? =
        adapter.withVersion2 { swagger ->
            val paths = swagger.paths.orEmpty().keys.filterNot {
                val pathSegments = it.split("/").filter { it.isNotEmpty() }
                pathSegments.filter { !PatternUtil.isPathVariable(it) && !PatternUtil.isLowerCaseAndHyphens(it) }.isEmpty()
            }
            if (paths.isNotEmpty()) Violation(description, paths) else null
        }

}
