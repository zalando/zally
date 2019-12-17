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
