package org.zalando.zally.util

data class ErrorResponse(
    val title: String? = null,
    val status: String? = null,
    val detail: String? = null
)
