package org.zalando.zally.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * Thrown when the specified ApiReview could not be found.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
class ApiReviewNotFoundException : RuntimeException()
