package de.zalando.zally.rule.zally

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

/**
 * Rule highlighting that x-zally-ignore should be used sparingly
 */
@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "H002",
        severity = Severity.HINT,
        title = "Avoid using x-zally-ignore extension."
)
class AvoidXZallyIgnoreRule {

    private val zallyIgnoreExtension = "x-zally-ignore"

    private val description = "Ignoring rules should be reserved for exceptional temporary circumstances"

    /**
     * Check the model doesn't use x-zally-ignore
     * @param root JsonNode root of the spec model
     * @return Violation iff x-zally-ignore is in use
     */
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
            if (node.has(zallyIgnoreExtension)) {
                val ignores = node.get(zallyIgnoreExtension)
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
