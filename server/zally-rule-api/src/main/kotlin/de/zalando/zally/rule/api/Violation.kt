package de.zalando.zally.rule.api

import com.fasterxml.jackson.core.JsonPointer
import java.util.Arrays.asList

data class Violation(
    val description: String,
    val pointer: JsonPointer
)
