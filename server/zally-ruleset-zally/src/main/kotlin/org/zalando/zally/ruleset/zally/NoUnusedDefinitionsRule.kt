package org.zalando.zally.ruleset.zally

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "S005",
    severity = Severity.SHOULD,
    title = "Do not leave unused definitions"
)
class NoUnusedDefinitionsRule {

    @Check(severity = Severity.SHOULD)
    fun checkSwagger(root: JsonNode): List<Violation> {

        val used = used(root) { node ->
            node["discriminator"]
                ?.asText()
                ?.let { prop ->
                    node["properties"]
                        ?.get(prop)
                        ?.get("enum")
                        ?.takeIf { it.isArray }
                        ?.toList()
                        ?.map { it.asText() }
                        ?.map {
                            "/definitions".toJsonPointer() + it.toEscapedJsonPointer()
                        }
                }
        }

        return emptyList<Violation>() +
            unused(root, "/definitions", "Unused definition", used) +
            unused(root, "/parameters", "Unused parameter", used) +
            unused(root, "/responses", "Unused response", used)
    }

    @Check(severity = Severity.SHOULD)
    fun checkOpenAPI(root: JsonNode): List<Violation> {

        val used = used(root) { node ->
            node["discriminator"]
                ?.get("mapping")
                ?.toList()
                ?.map { it.asText() }
                ?.map {
                    "/components/schemas".toJsonPointer() + it.toEscapedJsonPointer()
                }
        }

        return emptyList<Violation>() +
            unused(root, "/components/schemas", "Unused schema", used) +
            unused(root, "/components/responses", "Unused response", used) +
            unused(root, "/components/parameters", "Unused parameter", used) +
            unused(root, "/components/examples", "Unused example", used) +
            unused(root, "/components/requestBodies", "Unused request body", used) +
            unused(root, "/components/headers", "Unused header", used) +
            unused(root, "/components/links", "Unused link", used) +
            unused(root, "/components/callbacks", "Unused callback", used)
    }

    private fun used(
        node: JsonNode,
        discriminators: (JsonNode) -> List<JsonPointer>?
    ): Set<JsonPointer> = when {
        node.isArray -> node.flatMap { used(it, discriminators) }.toSet()
        node.isObject -> {
            val references = mutableSetOf<JsonPointer>()
            references += reference(node)
            references += discriminators(node).orEmpty()
            references += node.flatMap { used(it, discriminators) }
            references
        }
        else -> emptySet()
    }

    private fun reference(node: JsonNode): Sequence<JsonPointer> =
        sequenceOf(node["\$ref"])
            .filterNotNull()
            .map { it.asText() }
            .filter { it.startsWith("#") }
            .map { it.substring(1) }
            .map { it.toJsonPointer() }

    private fun unused(
        root: JsonNode,
        pointer: String,
        description: String,
        used: Set<JsonPointer>
    ): List<Violation> {
        val ptr = pointer.toJsonPointer()
        return root.at(ptr)
            ?.fieldNames()
            ?.asSequence()
            ?.map { ptr + it.toEscapedJsonPointer() }
            ?.minus(used)
            ?.map { Violation(description, it) }
            ?.toList()
            .orEmpty()
    }
}
