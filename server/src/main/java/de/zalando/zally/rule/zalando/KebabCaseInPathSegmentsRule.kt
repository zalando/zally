package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
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
    internal val lowerCaseHyphenSeparatedRegex = """^[a-z-]+$""".toRegex()

    @Check(severity = Severity.MUST)
    fun checkKebabCaseInPathSegments(context: Context): List<Violation> =
        context.api.paths.orEmpty().entries
            .filter { (path, _) ->
                path.split("/").filterNot { it.isEmpty() }
                    .any { segment -> !PatternUtil.isPathVariable(segment) && !segment.matches(lowerCaseHyphenSeparatedRegex) }
            }
            .map { (_, pathEntry) -> context.violation(description, pathEntry) }
}
