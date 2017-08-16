package de.zalando.zally.github

import org.kohsuke.github.GHCommitState
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
class ApiValidationController
constructor(private val githubService: GithubService) {

    @ResponseBody
    @PostMapping("/api-validation")
    @ResponseStatus(value = HttpStatus.ACCEPTED)
    fun validatePullRequest(@RequestBody payload: String,
                            @RequestHeader(value = "X-GitHub-Event") eventType: String,
                            @RequestHeader(value = "X-Hub-Signature") signature: String) {

        if (eventType != ApiValidationController.PULL_REQUEST_EVENT_NAME) {
            LOG.info("Received unsupported event: {}", eventType)
            return
        }

        val pullRequest = githubService.parsePayload(payload, signature)

        pullRequest.updateCommitState(GHCommitState.FAILURE, "https://127.0.0.1", "Hello", "Test")
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ApiValidationController::class.java)
        private val PULL_REQUEST_EVENT_NAME = "pull_request"
    }

}
