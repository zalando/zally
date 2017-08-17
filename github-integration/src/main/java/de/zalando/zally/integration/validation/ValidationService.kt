package de.zalando.zally.integration.validation

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.config.logger
import de.zalando.zally.integration.zally.ViolationType
import de.zalando.zally.integration.github.GithubService
import de.zalando.zally.integration.github.PullRequest
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import de.zalando.zally.integration.zally.ViolationType
import de.zalando.zally.integration.zally.ZallyService
import org.kohsuke.github.GHCommitState
import org.springframework.stereotype.Service


@Service
class ValidationService(private val githubService: GithubService,
                        private val zallyService: ZallyService,
                        private val validationRepository: ValidationRepository,
                        private val jsonObjectMapper: ObjectMapper) {


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

        val apiDefinition = swaggerFile.get()
        val validationResult = zallyService.validate(apiDefinition)

        storeValidationResults(pullRequest, apiDefinition, validationResult)

        val invalid = validationResult.violations?.any { it.violationType == ViolationType.MUST } ?: false
        if (invalid) {
            pullRequest.updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Got violations")
            return
        }

        pullRequest.updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "API passed all checks ${validationResult.violationsCount}")
    }

    private fun storeValidationResults(pullRequest: PullRequest, apiDefinitionString: String, validationResult: ApiDefinitionResponse) {
        val validation = Validation().apply {
            repositoryUrl = pullRequest.getRepositoryUrl()
            apiDefinition = apiDefinitionString
            violations = jsonObjectMapper.writeValueAsString(validationResult)
        }

        validationRepository.save(validation)
    }

}