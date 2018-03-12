package de.zalando.zally.rule.zally

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "H002",
        severity = Severity.HINT,
        title = "Avoid using x-zally-ignores extension."
)
class AvoidXZallyIgnoresRule {

    private val description = "Ignoring rules should be reserved for exceptional temporary circumstances"

    @Check(severity = Severity.HINT)
    fun validate(root: JsonNode): Violation? {
        val paths = recurse(root)
        return when {
            paths.isEmpty() -> null
            else -> Violation(description, paths)
        }
    }

    private fun recurse(node: JsonNode): List<String> {
        val paths = mutableListOf<String>()
        if (node.isObject) {
            if (node.has("x-zally-ignores")) {
                val ignores = node.get("x-zally-ignores")
                val path = when {
                    ignores.isArray -> "ignores rules " + ignores.joinToString(separator = ", ", transform = JsonNode::asText)
                    ignores.isValueNode -> "invalid ignores, expected list but found single value $ignores"
                    else -> "invalid ignores, expected list but found $ignores"
                }
                paths.add(path)
            }

            node.fields().forEach { (name, child) ->
                paths.addAll(recurse(child).map { "$name: $it" })
            }
        }
        return paths
    }
}
