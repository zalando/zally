package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
class ReverseAst internal constructor(private val objectsToNodes: Map<Any, Node>, private val pointersToNodes: Map<String, Node>) {

    fun getPointer(key: Any): JsonPointer? = objectsToNodes[key]?.pointer

    fun isIgnored(pointer: JsonPointer, ignoreValue: String): Boolean =
        isIgnored(this.pointersToNodes[pointer.toString()], ignoreValue)

    private fun isIgnored(node: Node?, ignoreValue: String): Boolean {
        return when {
            node != null && isIgnored(node.marker, ignoreValue) -> true
            node != null && node.hasChildren() && node.children.all { c -> isIgnored(c.marker, ignoreValue) } -> true
            else -> false
        }
    }

    private fun isIgnored(marker: Marker?, ignoreValue: String): Boolean =
        marker != null &&
        Marker.TYPE_X_ZALLY_IGNORE == marker.type &&
        marker.values.contains(ignoreValue)

    fun getIgnoreValues(pointer: JsonPointer): Collection<String> =
        getIgnoreValues(this.pointersToNodes[pointer.toString()])

    private fun getIgnoreValues(node: Node?): Collection<String> {
        val markers = node?.let { getIgnoreValues(node.marker) }.orEmpty()
        return when {
            node == null -> emptySet()
            markers.isNotEmpty() -> markers
            else -> node.children.flatMap { getIgnoreValues(it.marker) }.toSet()
        }
    }

    private fun getIgnoreValues(marker: Marker?): Collection<String> = when {
        marker != null && Marker.TYPE_X_ZALLY_IGNORE == marker.type -> marker.values
        else -> emptySet()
    }

    companion object {
        /**
         * Creates a new instance of ReverseAstBuilder from a Swagger or OpenApi object.
         *
         * @param root Swagger or OpenApi instance.
         * @return ReverseAstBuilder instance.
         */
        fun <T : Any> fromObject(root: T): ReverseAstBuilder<T> {
            return ReverseAstBuilder(root)
        }
    }
}
