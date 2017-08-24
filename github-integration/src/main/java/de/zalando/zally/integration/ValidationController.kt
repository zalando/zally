package de.zalando.zally.integration

import de.zalando.zally.integration.config.logger
import de.zalando.zally.integration.validation.ValidationService
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
    private val PULL_REQUEST_EVENT_NAME = "pull_request"

    val log by logger()

    @PostMapping("/github-webhook")
    @ResponseStatus(HttpStatus.ACCEPTED)
    fun validatePullRequest(@RequestBody payload: String,
                            @RequestHeader(value = "X-GitHub-Event") eventType: String,
                            @RequestHeader(value = "X-Hub-Signature") signature: String) {

        if (eventType != PULL_REQUEST_EVENT_NAME) {
            log.info("Received unsupported event: {}", eventType)
            return
        }

        log.info("Received webhook: {}", eventType)

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
