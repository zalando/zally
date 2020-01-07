package de.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer

/**
 * Constant representing the empty JsonPointer.
 */
val EMPTY_JSON_POINTER: JsonPointer = JsonPointer.compile(null)

/**
 * Compiles a valid json-pointer String into a JsonPointer instance.
 * @return the equivalent JsonPointer instance.
 * @throws IllegalArgumentException if the string is invalid.
 */
fun String.toJsonPointer(): JsonPointer = JsonPointer.compile(this)

/**
 * Escapes a string as a single element JsonPointer.
 * Escaping implemented according to https://tools.ietf.org/html/rfc6901
 * @return the JsonPointer.
 */
fun String.toEscapedJsonPointer(): JsonPointer = this
    .replace("~", "~0")
    .replace("/", "~1")
    .let { "/$it".toJsonPointer() }

/**
 * Concatenates two JsonPointer instances together.
 * @return the combined JsonPointer
 */
operator fun JsonPointer.plus(other: JsonPointer): JsonPointer = append(other)

/**
 * Convert an OpenAPI 3 JSON pointer to a Swagger 2 pointer.
 * @return Equivalent Swagger 2 JSON pointer or null if no conversion was possible..
 */
fun JsonPointer.toSwaggerJsonPointer(): JsonPointer? = toString()
    .let { ptr ->
        regexToReplacement
            .find { (regex, _) ->
                regex.matches(ptr)
            }
            ?.let { (regex, replacement) ->
                regex.replace(ptr, replacement).toJsonPointer()
            }
    }

private val regexToReplacement =
    listOf(
        "^/servers/.*$".toRegex() to "/basePath",
        "^/components/schemas/(.*)$".toRegex() to "/definitions/$1",
        "^/components/responses/(.*)$".toRegex() to "/responses/$1",
        "^/components/parameters/(.*)$".toRegex() to "/parameters/$1",
        "^/components/securitySchemes/(.*?)/flows/(implicit|password|clientCredentials|authorizationCode)/(.*)$".toRegex() to "/securityDefinitions/$1/$3",
        "^/components/securitySchemes/(.*)$".toRegex() to "/securityDefinitions/$1",
        "^/paths/(.+/responses/.+)/content/.+/(schema.*)$".toRegex() to "/paths/$1/$2",

        // VERB/responses/STATUS_CODE/content/MEDIA_TYPE --> VERB/responses/STATUS_CODE
        // Could also be VERB/produces but this information is lost from Swagger 2 to OpenAPI 3 conversion.
        "^/paths/(.+/responses/.+)/content/[^/]*$".toRegex() to "/paths/$1",

        // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
        "^/paths/(.*)/requestBody/content/[^/]*$".toRegex() to "/paths/$1/consumes"
    )
