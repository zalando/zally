package de.zalando.zally.rule.zally

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "H001",
        severity = Severity.HINT,
        title = "Base path can be extracted"
)
class ExtractBasePathRule {

    private val description = "All paths start with prefix '%s'. This prefix could be part of base path."

    @Check(severity = Severity.HINT)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys
        if (paths.size < 2) {
            return null
        }
        val commonPrefix = paths.reduce { s1, s2 -> findCommonPrefix(s1, s2) }
        return if (commonPrefix.isNotEmpty())
            Violation(description.format(commonPrefix), emptyList())
        else null
    }

    private fun findCommonPrefix(s1: String, s2: String): String {
        val parts1 = s1.split("/")
        val parts2 = s2.split("/")
        val (commonParts, _) = parts1.zip(parts2).takeWhile { (t1, t2) -> !t1.startsWith('{') && t1 == t2 }.unzip()
        return commonParts.joinToString("/")
    }
}
