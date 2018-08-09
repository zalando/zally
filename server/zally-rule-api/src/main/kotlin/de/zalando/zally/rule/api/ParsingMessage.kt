package de.zalando.zally.rule.api

import com.fasterxml.jackson.core.JsonPointer

data class ParsingMessage(
    val message: String,
    val pointer: JsonPointer
)

