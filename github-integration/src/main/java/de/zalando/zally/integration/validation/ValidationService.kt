package de.zalando.zally.integration.validation

import de.zalando.zally.integration.config.logger
import de.zalando.zally.integration.zally.ViolationType
import de.zalando.zally.integration.github.GithubService
import de.zalando.zally.integration.zally.ZallyService
import org.kohsuke.github.GHCommitState
import org.springframework.stereotype.Service


@Service
class ValidationService(private val githubService: GithubService,
                        private val zallyService: ZallyService) {


    fun validatePullRequest(payload: String, signature: String) {

        val pullRequest = githubService.parsePayload(payload, signature)

        if (!pullRequest.getConfiguration().isPresent) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Could not find zally configuration file")
            return
        }

        if (!pullRequest.isAPIChanged()) {
            pullRequest.updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "Neither configuration nor swagger file changed")
            return
        }

        val swaggerFile = pullRequest.getSwaggerFile()

        if (!swaggerFile.isPresent) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Could not find swagger file")
            return
        }

        val validate = zallyService.validate(swaggerFile.get())
        val invalid = validate.violations?.any { it.violationType == ViolationType.MUST } ?: false
        if (invalid) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Got violations")
            return
        }

        pullRequest.updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "API passed all checks ${validate.violationsCount}")
    }

}