package org.zalando.zally.exception

import org.springframework.http.HttpStatusCode

class InaccessibleResourceUrlException(
    message: String,
    val httpStatus: HttpStatusCode,
) : RuntimeException(message)
