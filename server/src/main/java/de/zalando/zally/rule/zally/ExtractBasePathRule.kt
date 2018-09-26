package de.zalando.zally.rule.zally

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "H001",
        severity = Severity.HINT,
        title = "Base path can be extracted"
)
class ExtractBasePathRule {

    private val description = "All paths start with prefix '%s'. This prefix could be part of base path."

    @Check(severity = Severity.HINT)
    fun validate(context: Context): List<Violation> {
        val paths = context.api.paths?.keys.orEmpty()
        val prefix = paths.reduce { s1, s2 -> findCommonPrefix(s1, s2) }
        return when {
            paths.size < 2 || prefix.isEmpty() -> emptyList()
            else -> listOf(Violation(description.format(prefix), emptyList(), JsonPointer.compile("/paths")))
        }
    }

    private fun findCommonPrefix(s1: String, s2: String): String {
        val parts1 = s1.split("/")
        val parts2 = s2.split("/")
        val (commonParts, _) = parts1.zip(parts2).takeWhile { (t1, t2) -> !t1.startsWith('{') && t1 == t2 }.unzip()
        return commonParts.joinToString("/")
    }
}
