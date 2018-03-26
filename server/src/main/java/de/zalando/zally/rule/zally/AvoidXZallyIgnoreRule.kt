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

    private val description = "Ignoring rules should be reserved for exceptional temporary circumstances"

    private val xZallyIgnore = "x-zally-ignore"

    /**
     * Check the model doesn't use x-zally-ignore
     * @param root JsonNode root of the spec model
     * @return Violation iff x-zally-ignore is in use
     */
    @Check(severity = Severity.HINT)
    fun validate(root: JsonNode): Violation? {
        val paths = validateTree(root)
        return when {
            paths.isEmpty() -> null
            else -> Violation(description, paths)
        }
    }

    private fun validateTree(node: JsonNode): List<String> {
        return when {
            node.isObject -> validateXZallyIgnore(node) + validateChildren(node)
            else -> emptyList()
        }
    }

    private fun validateXZallyIgnore(node: JsonNode): List<String> {
        return when {
            node.has(xZallyIgnore) -> {
                val ignores = node.get(xZallyIgnore)
                when {
                    ignores.isArray -> listOf("ignores rules " + ignores.joinToString(separator = ", ", transform = JsonNode::asText))
                    ignores.isValueNode -> listOf("invalid ignores, expected list but found single value $ignores")
                    else -> listOf("invalid ignores, expected list but found $ignores")
                }
            }
            else -> emptyList()
        }
    }

    private fun validateChildren(node: JsonNode): List<String> {
        return node.fields().asSequence().toList().flatMap { (name, child) ->
            validateTree(child).map { "$name: $it" }
        }
    }
}
