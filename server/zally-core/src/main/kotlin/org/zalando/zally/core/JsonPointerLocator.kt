package org.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.nodes.CollectionNode
import org.yaml.snakeyaml.nodes.MappingNode
import org.yaml.snakeyaml.nodes.Node
import org.yaml.snakeyaml.nodes.ScalarNode
import java.io.StringReader

/**
 * Identifies line locations from JsonPointers.
 */
class JsonPointerLocator(contents: String) {

    private val yaml = when {
        isJson(contents) -> null
        else -> Yaml().compose(StringReader(contents))
    }

    private fun isJson(contents: String): Boolean = contents.trim { it <= ' ' }.startsWith("{")

    /**
     * Identify the range of line making up an element indicated by a JsonPointer
     */
    fun locate(pointer: JsonPointer): IntRange? = yaml?.let { locate(pointer, yaml) }

    private fun locate(pointer: JsonPointer, node: Node, parent: Node = node): IntRange = when (node) {
        is MappingNode -> locateMapping(pointer, node)
        is CollectionNode<*> -> locateCollection(pointer, node)
        else -> null
    } ?: parent.startMark.line + 1..node.endMark.line + 1

    private fun locateMapping(pointer: JsonPointer, node: MappingNode): IntRange? = node.value
        .mapNotNull { tuple ->
            val keyNode = tuple.keyNode
            if (keyNode is ScalarNode && pointer.matchesProperty(keyNode.value)) {
                locate(pointer.tail(), tuple.valueNode, keyNode)
            } else null
        }
        .firstOrNull()

    private fun locateCollection(pointer: JsonPointer, node: CollectionNode<*>): IntRange? = node.value
        .mapIndexedNotNull { index, any ->
            if (any is Node && pointer.matchesElement(index)) {
                locate(pointer.tail(), any)
            } else null
        }
        .firstOrNull()
}
