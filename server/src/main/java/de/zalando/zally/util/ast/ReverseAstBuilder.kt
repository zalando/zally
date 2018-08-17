package de.zalando.zally.util.ast

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.core.JsonPointer
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Deque
import java.util.IdentityHashMap
import java.util.LinkedList
import kotlin.Comparator
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

class ReverseAstBuilder<T> internal constructor(root: T) {
    private val extensionMethodNames = HashSet<String>()

    private val nodes = LinkedList<Node>()
    private val objectsToNodes = IdentityHashMap<Any, Node>()
    private val pointersToNodes = HashMap<String, Node>()

    class ReverseAstException internal constructor(message: String, cause: Throwable) : Exception(message, cause)

    init {
        nodes.push(Node(root as Any, JsonPointers.EMPTY, null))
    }

    fun withExtensionMethodNames(vararg names: String): ReverseAstBuilder<T> {
        this.extensionMethodNames.addAll(listOf(*names))
        return this
    }

    /**
     * Construct a new ReverseAst instance from the root object in this builder.
     * Traverses a Swagger or OpenApi object tree and constructs a map of object nodes to meta information objects.
     *
     * @return A new ReverseAst instance.
     * @throws ReverseAstException If an error occurs during reflection.
     */
    @Throws(ReverseAstException::class)
    fun build(): ReverseAst {
        while (!nodes.isEmpty()) {
            val node = nodes.pop()
            if (objectsToNodes.containsKey(node.`object`)) {
                continue
            }
            if (!Util.PRIMITIVES.contains(node.`object`.javaClass)) {
                val children = when {
                    node.`object` is Map<*, *> -> handleMap(node.`object`, node.pointer, node.marker)
                    node.`object` is List<*> -> handleList(node.`object`, node.pointer, node.marker)
                    node.`object` is Set<*> -> handleSet(node.`object`, node.pointer, node.marker)
                    node.`object` is Array<*> -> handleArray(node.`object`, node.pointer, node.marker)
                    else -> handleObject(node.`object`, node.pointer, node.marker)
                }
                for (child in children) {
                    nodes.push(child)
                }
                node.children.addAll(children)
            }
            if (!node.skip) {
                objectsToNodes[node.`object`] = node
                pointersToNodes[node.pointer.toString()] = node
            }
        }
        return ReverseAst(objectsToNodes, pointersToNodes)
    }

    private fun handleMap(map: Map<*, *>, pointer: JsonPointer, defaultMarker: Marker?): Deque<Node> {
        val nodes = LinkedList<Node>()
        val marker = getMarker(map) ?: defaultMarker

        for ((key, value) in map) {
            if (key is String && value != null) {
                val newPointer = pointer.append(JsonPointers.escape(key))
                nodes.push(Node(value, newPointer, marker))
            }
        }
        return nodes
    }

    private fun handleList(list: List<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> {
        return handleArray(list.toTypedArray(), pointer, marker)
    }

    private fun handleSet(set: Set<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> {
        return handleArray(set.toTypedArray(), pointer, marker)
    }

    private fun handleArray(objects: Array<*>, pointer: JsonPointer, marker: Marker?): Deque<Node> {
        val nodes = LinkedList<Node>()

        for (i in objects.indices) {
            val value = objects[i]
            if (value != null) {
                val newPointer = pointer.append(JsonPointers.escape(i.toString()))
                nodes.push(Node(value, newPointer, marker))
            }
        }
        return nodes
    }

    @Throws(ReverseAstException::class)
    private fun handleObject(`object`: Any, pointer: JsonPointer, defaultMarker: Marker?): Deque<Node> {
        val nodes = LinkedList<Node>()
        val marker = getMarker(`object`) ?: defaultMarker

        for (m in traversalMethods(`object`.javaClass)) {
            val name = m.name
            try {
                val value = m.invoke(`object`)
                if (value != null) {
                    if (m.isAnnotationPresent(JsonAnyGetter::class.java)) {
                        // A `JsonAnyGetter` method is simply a wrapper for nested properties.
                        // We must not use the method name but re-use the current pointer.
                        nodes.push(Node(value, pointer, marker, /* skip */true))
                    } else {
                        val newPointer = pointer.append(JsonPointers.escape(m))
                        nodes.push(Node(value, newPointer, marker))
                    }
                }
            } catch (e: ReflectiveOperationException) {
                val message = String.format("Error invoking %s on %s at path %s", name, `object`.javaClass, pointer)
                throw ReverseAstException(message, e)
            }
        }
        return nodes
    }

    private fun getMarker(map: Map<*, *>): Marker? =
        getVendorExtensions(map, Marker.TYPE_X_ZALLY_IGNORE)
            ?.let { Marker(Marker.TYPE_X_ZALLY_IGNORE, it) }

    @Throws(ReverseAstException::class)
    private fun getMarker(`object`: Any): Marker? =
        getVendorExtensions(`object`, Marker.TYPE_X_ZALLY_IGNORE)
            ?.let { Marker(Marker.TYPE_X_ZALLY_IGNORE, it) }

    @Throws(ReverseAstBuilder.ReverseAstException::class)
    private fun getVendorExtensions(`object`: Any, extensionName: String): Collection<String>? {
        if (`object` is Map<*, *>) {
            return getVendorExtensions(`object`, extensionName)
        }
        for (m in traversalMethods(`object`.javaClass)) {
            if (extensionMethodNames.contains(m.name)) {
                try {
                    val extensions = m.invoke(`object`)
                    if (extensions is Map<*, *>) {
                        return getVendorExtensions(extensions, extensionName)
                    }
                } catch (e: ReflectiveOperationException) {
                    throw ReverseAstException("Error getting extensions.", e)
                }
            }
        }
        return null
    }

    private fun getVendorExtensions(map: Map<*, *>, extensionName: String): Collection<String>? {
        if (map.containsKey(extensionName)) {
            val value = map[extensionName]
            if (value is String) {
                return setOf(value)
            }
            if (value is Collection<*>) {
                return value.map { it.toString() }.toSet()
            }
        }
        return null
    }

    companion object {
        fun traversalMethods(clazz: Class<*>) = clazz
            .methods
            .filter {
                it.name.startsWith("get") &&
                    it.name != "getClass" &&
                    it.name != "getDeclaringClass" &&
                    it.parameterCount == 0 &&
                    Modifier.isPublic(it.modifiers) &&
                    !it.isAnnotationPresent(JsonIgnore::class.java)
            }
            .sortedWith(Comparator
                .comparing { method: Method -> method.name == "getPaths" }
                .thenComparing { method: Method -> method.name })
    }
}
