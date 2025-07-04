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

    @ExceptionHandler(MissingApiDefinitionException::class)
    fun handleMissingApiDefinitionException(
        exception: MissingApiDefinitionException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }

    @ExceptionHandler(InaccessibleResourceUrlException::class)
    fun handleUnaccessibleResourceUrlException(
        exception: InaccessibleResourceUrlException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(exception.httpStatus, exception, request)
    }

    @ExceptionHandler(InsufficientTimeIntervalParameterException::class)
    fun handleInsufficientTimeIntervalParameterException(
        exception: InsufficientTimeIntervalParameterException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }

    @ExceptionHandler(TimeParameterIsInTheFutureException::class)
    fun handleTimeParameterIsInTheFutureException(
        exception: TimeParameterIsInTheFutureException,
        request: NativeWebRequest
    ): ResponseEntity<Problem> {
        return create(HttpStatus.BAD_REQUEST, exception, request)
    }
}
