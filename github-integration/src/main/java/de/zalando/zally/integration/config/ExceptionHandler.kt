package de.zalando.zally.integration.config

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class ExceptionHandler {

    val log by logger()

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleControllerException(request: HttpServletRequest, ex: Throwable): ResponseEntity<*> {
        log.error("request failed", ex)
        return ResponseEntity<Any>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

}
