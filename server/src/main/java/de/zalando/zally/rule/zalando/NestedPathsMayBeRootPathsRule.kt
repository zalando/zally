package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isPathVariable
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "145",
        severity = Severity.MAY,
        title = "Consider Using (Non-) Nested URLs"
)
class NestedPathsMayBeRootPathsRule {
    private val description = "Nested paths / URLs may be top-level resource"

    @Check(severity = Severity.MAY)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filter {
            val pathSegments = it.split("/".toRegex())
            // we are only interested in paths that have sub-resource followed by a param: /path1/{param1}/path2/{param2}
            pathSegments.size > 4 && isPathVariable(pathSegments.last())
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }
}
