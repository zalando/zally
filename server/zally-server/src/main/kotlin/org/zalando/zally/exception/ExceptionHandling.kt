package org.zalando.zally.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.NativeWebRequest
import org.zalando.problem.Problem
import org.zalando.problem.spring.web.advice.ProblemHandling
import org.zalando.problem.spring.web.advice.SpringAdviceTrait

@ControllerAdvice
class ExceptionHandling : ProblemHandling, SpringAdviceTrait {

    @ExceptionHandler
    fun handleMissingApiDefinitionException(
        exception: MissingApiDefinitionException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }

    @ExceptionHandler
    fun handleUnaccessibleResourceUrlException(
        exception: InaccessibleResourceUrlException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(exception.httpStatus, exception, request)
    }

    @ExceptionHandler
    fun handleUnsufficientTimeIntervalParameterException(
        exception: InsufficientTimeIntervalParameterException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }

    @ExceptionHandler
    fun handleUnsufficientTimeIntervalParameterException(
        exception: TimeParameterIsInTheFutureException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }
}
