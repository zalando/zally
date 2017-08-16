package de.zalando.zally.github

import de.zalando.zally.github.dto.ViolationType
import de.zalando.zally.github.util.logger
import org.kohsuke.github.GHCommitState
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RequestHeader
import javax.servlet.http.HttpServletRequest

@RestController
class ApiValidationController(private val githubService: GithubService,
                              private val zallyService: ZallyService) {

    val log by logger()

    @ResponseBody
    @PostMapping("/api-validation")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    fun validatePullRequest(@RequestBody payload: String,
                            @RequestHeader(value = "X-GitHub-Event") eventType: String,
                            @RequestHeader(value = "X-Hub-Signature") signature: String) {

        if (eventType != ApiValidationController.PULL_REQUEST_EVENT_NAME) {
            log.info("Received unsupported event: {}", eventType)
            return
        }

        log.info("Received webhook: {}", eventType)

        val pullRequest = githubService.parsePayload(payload, signature)

        if (!pullRequest.getConfiguration().isPresent) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Hello", "Test")
            return
        }

        val swaggerFile = pullRequest.getSwaggerFile()
        if (!swaggerFile.isPresent) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Hello", "Test")
            return
        }

        val validate = zallyService.validate(swaggerFile.get())
        val invalid = validate.violations?.any { it.violationType == ViolationType.MUST } ?: false
        if (invalid) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Hello", "Test")
            return
        }

        pullRequest.updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "Hello", "Test")
        log.info("Finished webhook processing")
    }

    @ExceptionHandler(Exception::class)
    @ResponseBody
    fun handleControllerException(request: HttpServletRequest, ex: Throwable): ResponseEntity<*> {
        log.error("failed", ex)
        return ResponseEntity<Any>(HttpStatus.INTERNAL_SERVER_ERROR)
    }

    companion object {
        private val PULL_REQUEST_EVENT_NAME = "pull_request"
    }

}
