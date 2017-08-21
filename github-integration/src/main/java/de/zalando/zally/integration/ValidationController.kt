package de.zalando.zally.integration

import de.zalando.zally.integration.config.logger
import de.zalando.zally.integration.validation.ValidationService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

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

}
