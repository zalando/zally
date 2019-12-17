package de.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import java.lang.reflect.Method

/**
 * Utility to convert OpenAPI 3 JSON pointers to Swagger 2 pointers.
 */
object JsonPointers {

    private val regexToReplacement =
        listOf(
            "^/servers/.*$" to "/basePath",
            "^/components/schemas/(.*)$" to "/definitions/$1",
            "^/components/responses/(.*)$" to "/responses/$1",
            "^/components/parameters/(.*)$" to "/parameters/$1",
            "^/components/securitySchemes/(.*?)/flows/(implicit|password|clientCredentials|authorizationCode)/(.*)$" to "/securityDefinitions/$1/$3",
            "^/components/securitySchemes/(.*)$" to "/securityDefinitions/$1",
            "^/paths/(.+/responses/.+)/content/.+/(schema.*)$" to "/paths/$1/$2",

            // VERB/responses/STATUS_CODE/content/MEDIA_TYPE --> VERB/responses/STATUS_CODE
            // Could also be VERB/produces but this information is lost from Swagger 2 to OpenAPI 3 conversion.
            "^/paths/(.+/responses/.+)/content/[^/]*$" to "/paths/$1",

            // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
            "^/paths/(.*)/requestBody/content/[^/]*$" to "/paths/$1/consumes"
        )
            .map { it.first.toRegex() to it.second }

    /**
     * Convert an OpenAPI 3 JSON pointer to a Swagger 2 pointer.
     *
     * @param pointer OpenAPI 3 JSON pointer.
     * @return Equivalent Swagger 2 JSON pointer or the original OpenAPI 3 pointer..
     */
    fun convertPointer(pointer: JsonPointer): JsonPointer = pointer
        .toString()
        .let { ptr ->
            regexToReplacement
                .find { (regex, _) ->
                    regex.matches(ptr)
                }
                ?.let { (regex, replacement) ->
                    regex.replace(ptr, replacement).toJsonPointer()
                }
                ?: pointer
        }

    fun escape(method: Method, vararg arguments: Any): JsonPointer =
        escape(method.name
            .let { if (it.startsWith("get")) it.drop(3) else it }
            .decapitalize()
            .let { if (arguments.isNotEmpty()) it + arguments[0] else it })

    // https://tools.ietf.org/html/rfc6901
    fun escape(unescaped: String): JsonPointer = unescaped
        .replace("~", "~0")
        .replace("/", "~1")
        .let { "/$it".toJsonPointer() }
}
