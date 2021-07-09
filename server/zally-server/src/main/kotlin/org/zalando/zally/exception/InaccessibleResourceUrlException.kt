package org.zalando.zally.exception

import org.springframework.http.HttpStatus

class InaccessibleResourceUrlException(message: String, val httpStatus: HttpStatus) : RuntimeException(message)
