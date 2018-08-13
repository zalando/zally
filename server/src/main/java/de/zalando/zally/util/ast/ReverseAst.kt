package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer

/**
 * ReverseAst holds meta information for nodes of a Swagger or OpenApi object.
 */
class ReverseAst internal constructor(private val objectsToNodes: Map<Any, Node>, private val pointersToNodes: Map<String, Node>) {

    fun getPointer(key: Any): JsonPointer? {
        val node = this.objectsToNodes[key]
        return node?.pointer
    }

    fun isIgnored(pointer: JsonPointer, ignoreValue: String): Boolean {
        return isIgnored(this.pointersToNodes[pointer.toString()], ignoreValue)
    }

    private fun isIgnored(node: Node?, ignoreValue: String): Boolean {
        if (node == null) {
            return false
        }
        val ignored = isIgnored(node.marker, ignoreValue)
        return ignored || node.hasChildren() && node.children.parallelStream().allMatch { c -> isIgnored(c.marker, ignoreValue) }
    }

    private fun isIgnored(marker: Marker?, ignoreValue: String): Boolean {
        return marker != null && Marker.TYPE_X_ZALLY_IGNORE == marker.type && marker.values.contains(ignoreValue)
    }

    fun getIgnoreValues(pointer: JsonPointer): Collection<String> {
        return getIgnoreValues(this.pointersToNodes[pointer.toString()])
    }

    private fun getIgnoreValues(node: Node?): Collection<String> {
        if (node == null) {
            return emptySet()
        }
        val markers = getIgnoreValues(node.marker)
        return if (!markers.isEmpty()) {
            markers
        } else node
                .children
                .flatMap { child -> getIgnoreValues(child.marker) }
                .toSet()
    }

    private fun getIgnoreValues(marker: Marker?): Collection<String> {
        return if (marker != null && Marker.TYPE_X_ZALLY_IGNORE == marker.type) {
            marker.values
        } else emptySet()
    }

    companion object {
        /**
         * Creates a new instance of ReverseAstBuilder from a Swagger or OpenApi object.
         *
         * @param root Swagger or OpenApi instance.
         * @return ReverseAstBuilder instance.
         */
        fun <T> fromObject(root: T): ReverseAstBuilder<T> {
            return ReverseAstBuilder(root)
        }
    }
}
