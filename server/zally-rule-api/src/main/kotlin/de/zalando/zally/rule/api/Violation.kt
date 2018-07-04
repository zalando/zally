package de.zalando.zally.rule.api

import com.fasterxml.jackson.core.JsonPointer
import java.util.Arrays.asList

data class Violation(
    val description: String,
    @Deprecated("Use `pointer` instead.") val paths: List<String>,
    val pointer: JsonPointer? = null
) {
    @Deprecated("Use JsonPointer constructor instead.")
    constructor(description: String, paths: List<String>) : this(description, paths, null)
    @Deprecated("Use JsonPointer constructor instead.")
    constructor(description: String, vararg paths: String) : this(description, asList(*paths))
    constructor(description: String, pointer: JsonPointer) : this(description, listOf(pointer.toString()), pointer)
}
