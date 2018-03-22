package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isPathVariable

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "143",
    severity = Severity.MUST,
    title = "Every Second Path Level To Be Parameter"
)
class EverySecondPathLevelParameterRule {
    private val description = "Every second path level must be a path parameter"

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? =
        adapter.withVersion2 { swagger ->
            val paths = swagger.paths.orEmpty().keys.filterNot {
                val pathSegments = it.split("/").filter { it.isNotEmpty() }
                pathSegments.filterIndexed { i, segment -> isPathVariable(segment) == (i % 2 == 0) }.isEmpty()
            }
            if (paths.isNotEmpty()) Violation(description, paths) else null
        }
}
