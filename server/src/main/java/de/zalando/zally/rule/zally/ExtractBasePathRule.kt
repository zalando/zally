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

    @Check(severity = Severity.HINT)
    fun validate(context: Context): List<Violation> {
        val paths = context.api.paths?.keys.orEmpty()
        val prefix = paths.reduce { s1, s2 -> findCommonPrefix(s1, s2) }
        return when {
            paths.size < 2 || prefix.isEmpty() -> emptyList()
            context.isOpenAPI3() -> violations(prefix, "servers' urls")
            else -> violations(prefix, "basePath")
        }
    }

    fun violations(prefix: String, target: String) =
        listOf(
            Violation(
                "All paths start with prefix '$prefix' which could be part of $target.",
                JsonPointer.compile("/paths")
            )
        )

    private fun findCommonPrefix(s1: String, s2: String): String {
        val parts1 = s1.split("/")
        val parts2 = s2.split("/")
        val (commonParts, _) = parts1.zip(parts2).takeWhile { (t1, t2) -> !t1.startsWith('{') && t1 == t2 }.unzip()
        return commonParts.joinToString("/")
    }
}
