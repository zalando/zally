package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.PatternUtil.isPathVariable
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "143",
        severity = Severity.MUST,
        title = "Every Second Path Level To Be Parameter"
)
class EverySecondPathLevelParameterRule : AbstractRule() {
    private val description = "Every second path level must be a path parameter"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filterNot {
            val pathSegments = it.split("/").filter { it.isNotEmpty() }
            pathSegments.filterIndexed { i, segment -> isPathVariable(segment) == (i % 2 == 0) }.isEmpty()
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }
}
