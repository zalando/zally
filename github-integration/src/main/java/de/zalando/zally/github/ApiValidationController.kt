package de.zalando.zally.github

import de.zalando.zally.github.util.logger
import org.kohsuke.github.GHCommitState
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class ApiValidationController(private val githubService: GithubService) {

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

        val pullRequest = githubService.parsePayload(payload, signature)

        pullRequest.updateCommitState(GHCommitState.FAILURE, "https://127.0.0.1", "Hello", "Test")
    }

    companion object {
        private val PULL_REQUEST_EVENT_NAME = "pull_request"
    }

}
