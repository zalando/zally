package org.zalando.zally.exception

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Turns every exception into an `application/problem+json` body with exactly `title`, `status` and `detail`
 * (`detail` is left out when there is none), the shape clients have always received.
 *
 * Replaces `problem-spring-web`. Deliberately not Spring's `ProblemDetail`, which adds `type` and `instance`.
 */
@ControllerAdvice
class ExceptionHandling : ResponseEntityExceptionHandler() {
    @ExceptionHandler(MissingApiDefinitionException::class, InsufficientTimeIntervalParameterException::class, TimeParameterIsInTheFutureException::class)
    fun handleBadRequest(exception: RuntimeException): ResponseEntity<Any> = problem(HttpStatus.BAD_REQUEST, exception.message)

    @ExceptionHandler
    fun handleInaccessibleResourceUrl(exception: InaccessibleResourceUrlException): ResponseEntity<Any> = problem(exception.httpStatus, exception.message)

    @ExceptionHandler
    fun handleNotFound(exception: ApiReviewNotFoundException): ResponseEntity<Any> = problem(HttpStatus.NOT_FOUND, null)

    @ExceptionHandler
    fun handleAnyOther(exception: Exception): ResponseEntity<Any> {
        // The container would have logged this; once the advice answers, nobody else does.
        logger.error("Unexpected error while handling a request", exception)
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, exception.message)
    }

    /**
     * Unmapped routes keep Spring Boot's default error body (`timestamp`, `status`, `error`, `path`) as before:
     * hand them back to the servlet container's error page instead of turning them into a problem.
     */
    override fun handleNoResourceFoundException(
        ex: NoResourceFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? = sendContainerError(request, status)

    override fun handleNoHandlerFoundException(
        ex: NoHandlerFoundException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? = sendContainerError(request, status)

    // `sendError` hands the request to the container's ERROR dispatch (`/error`), which Boot answers with its default body.
    // OAuthConfiguration must let that dispatch through, see `dispatcherTypeMatchers(DispatcherType.ERROR)`. The golden
    // master and ProductionSecurityTest fail if either side changes.
    private fun sendContainerError(
        request: WebRequest,
        status: HttpStatusCode,
    ): ResponseEntity<Any>? {
        (request as ServletWebRequest).response?.sendError(status.value())
        return null
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        if (statusCode.is5xxServerError) logger.error("Server error while handling a request", ex)
        return problem(statusCode, ex.message, headers)
    }

    private fun problem(
        status: HttpStatusCode,
        detail: String?,
        headers: HttpHeaders = HttpHeaders(),
    ): ResponseEntity<Any> {
        val title = HttpStatus.resolve(status.value())?.reasonPhrase ?: status.toString()
        val body = linkedMapOf<String, Any>("title" to title, "status" to status.value())
        detail?.let { body["detail"] = it }
        val responseHeaders =
            HttpHeaders().apply {
                putAll(headers)
                contentType = MediaType.APPLICATION_PROBLEM_JSON
            }
        return ResponseEntity(body, responseHeaders, status)
    }
}
