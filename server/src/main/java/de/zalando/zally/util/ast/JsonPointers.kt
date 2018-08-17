package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import java.lang.reflect.Method
import java.util.Objects

/**
 * Utility to convert OpenAPI 3 JSON pointers to Swagger 2 pointers.
 */
object JsonPointers {

    val EMPTY: JsonPointer = JsonPointer.compile("")

    private val regexToReplacement = listOf(
            "^/servers/.*$" to "/basePath",
            "^/components/schemas/(.*)$" to "/definitions/$1",
            "^/components/responses/(.*)$" to "/responses/$1",
            "^/components/parameters/(.*)$" to "/parameters/$1",
            "^/components/securitySchemes/(.*)$" to "/securityDefinitions/$1",
            "^/paths/(.+/responses/.+)/content/.+/(schema.*)$" to "/paths/$1/$2",

            // VERB/responses/STATUS_CODE/content/MEDIA_TYPE --> VERB/responses/STATUS_CODE
            // Could also be VERB/produces but this information is lost from Swagger 2 to OpenAPI 3 conversion.
            "^/paths/(.+/responses/.+)/content/[^/]*$" to "/paths/$1",

            // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
            "^/paths/(.*)/requestBody/content/[^/]*$" to "/paths/$1/consumes")
            .map { it.first.toRegex() to it.second }

    /**
     * Convert an OpenAPI 3 JSON pointer to a Swagger 2 pointer.
     *
     * @param pointer OpenAPI 3 JSON pointer.
     * @return Equivalent Swagger 2 JSON pointer or null.
     */
    fun convertPointer(pointer: JsonPointer?): JsonPointer? = pointer
        ?.let { ptr ->
            val ptrString = ptr.toString()
            regexToReplacement
                .filter { (regex, _) ->
                    regex.matches(ptrString)
                }
                .map { (regex, replacement) ->
                    regex.replace(ptrString, replacement)
                }
                .firstOrNull()
                ?.let { JsonPointer.compile(it) }
        }

    internal fun escape(method: Method, vararg arguments: Any): JsonPointer {
        var name = method.name
        if (name.startsWith("get")) {
            name = name.substring(3)
        }
        if (name.isNotEmpty()) {
            name = name.substring(0, 1).toLowerCase() + name.substring(1)
        }
        if (arguments.isNotEmpty()) {
            name += Objects.toString(arguments[0])
        }
        return escape(name)
    }

    internal fun escape(unescaped: String): JsonPointer {
        // https://tools.ietf.org/html/rfc6901
        val escaped = "/" + unescaped
                .replace("~", "~0")
                .replace("/", "~1")
        return JsonPointer.compile(escaped)
    }
}
