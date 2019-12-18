package de.zalando.zally.core.ast

import com.fasterxml.jackson.core.JsonPointer

/**
 * A stack node for tree-traversal.
 */
internal data class Node(
    val obj: Any,
    val pointer: JsonPointer,
    val marker: Marker?,
    val skip: Boolean = false,
    val children: MutableList<Node> = mutableListOf()
) {
    fun hasChildren(): Boolean = children.isNotEmpty()
}
