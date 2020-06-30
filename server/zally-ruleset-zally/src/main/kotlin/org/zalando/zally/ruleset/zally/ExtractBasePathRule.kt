package org.zalando.zally.ruleset.zally

import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

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
        if (paths.size < 2) {
            return emptyList()
        }
        val prefix = paths.reduce { s1, s2 -> findCommonPrefix(s1, s2) }
        return when {
            prefix.isEmpty() -> emptyList()
            context.isOpenAPI3() -> violations(prefix, "servers' urls")
            else -> violations(prefix, "basePath")
        }
    }

    private fun violations(prefix: String, target: String) = listOf(
        Violation(
            "All paths start with prefix '$prefix' which could be part of $target.",
            "/paths".toJsonPointer()
        )
    )

    private fun findCommonPrefix(s1: String, s2: String): String {
        val parts1 = s1.split("/")
        val parts2 = s2.split("/")
        val (commonParts, _) = parts1.zip(parts2).takeWhile { (t1, t2) -> !t1.startsWith('{') && t1 == t2 }.unzip()
        return commonParts.joinToString("/")
    }
}
