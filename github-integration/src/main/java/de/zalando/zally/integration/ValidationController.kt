package de.zalando.zally.integration

import de.zalando.zally.integration.validation.ValidationService
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class ValidationController(private val validationService: ValidationService) {

    private val log = KotlinLogging.logger {}

    @PostMapping("/github-webhook", headers = arrayOf("X-GitHub-Event=pull_request"))
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun validatePullRequest(
        @RequestBody payload: String,
        @RequestHeader(value = "X-Hub-Signature") signature: String
    ) {

        log.info("Received pull request event on webhook")

        validationService.validatePullRequest(payload, signature)

        log.info("Finished webhook processing")
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleControllerException(request: HttpServletRequest, ex: Throwable): ResponseEntity<*> {
        log.error("request failed", ex)
        return ResponseEntity<Any>(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
