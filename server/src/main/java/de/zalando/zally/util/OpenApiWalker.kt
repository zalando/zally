package de.zalando.zally.util

import java.util.*
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberFunctions

object OpenApiWalker {
    fun walk(o: Any): Map<Any, String> {
        val map = IdentityHashMap<Any, String>()
        walk(o, "#", map)
        return map
    }

    private fun walk(o: Any, path: String, map: MutableMap<Any, String>) {
        map[o] = path
        when (o) {
            is String -> return
            is Number -> return
            is Boolean -> return
            is Map<*, *> -> {
                for ((key, value) in o) {
                    if (key is String && value != null) {
                        val name = "$path/${rfc6901Encode(key)}"
                        walk(value, name, map)
                    }
                }
            }
            is List<*> -> return
            // `Cloneable` objects cannot be reflected upon: https://youtrack.jetbrains.com/issue/KT-22923
            !is Cloneable -> {
                for (p in o.javaClass.kotlin.memberFunctions.filter {
                    // "getters" start with "get", have no own method arguments and are `public`
                    it.name.startsWith("get") && it.parameters.size == 1 && it.visibility == KVisibility.PUBLIC
                }) {
                    val value = p.call(o)
                    if (value != null) {
                        val name = getterNameToPathName(p.name)
                        walk(value, "$path/$name", map)
                    }
                }
            }
            else -> return
        }
    }

    // https://tools.ietf.org/html/rfc6901
    private fun rfc6901Encode(s: String) = s.replace("~", "~0").replace("/", "~1")

    private fun getterNameToPathName(name: String): String {
        val s = name.replace("get", "")
        return s.substring(0, 1).toLowerCase() + s.substring(1)
    }
}

