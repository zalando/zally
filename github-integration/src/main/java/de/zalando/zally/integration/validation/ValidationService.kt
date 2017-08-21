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
import org.kohsuke.github.GHCommitState.ERROR
import org.kohsuke.github.GHCommitState.SUCCESS
import org.springframework.stereotype.Service


@Service
class ValidationService(private val githubService: GithubService,
                        private val zallyService: ZallyService,
                        private val validationRepository: ValidationRepository,
                        private val jsonObjectMapper: ObjectMapper) {


    fun validatePullRequest(payload: String, signature: String) {

        val pullRequest = githubService.parsePayload(payload, signature)

        if (!pullRequest.getConfiguration().isPresent) {
            storeResultAndUpdateStatus(
                    RequestStatus.error("Could not find zally configuration file"), pullRequest, null, null)
            return
        }

        if (!pullRequest.isAPIChanged()) {
            pullRequest.updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "Neither configuration nor swagger file changed")
            return
        }

        val swaggerFile = pullRequest.getSwaggerFile()

        if (!swaggerFile.isPresent) {
            storeResultAndUpdateStatus(
                    RequestStatus.error( "Could not find swagger file"), pullRequest, null, null)
            return
        }

        val apiDefinition = swaggerFile.get()
        val validationResult = zallyService.validate(apiDefinition)
        val invalid = validationResult.violations?.any { it.violationType == ViolationType.MUST } ?: false
        if (invalid) {
            storeResultAndUpdateStatus(
                    RequestStatus.error( "Got violations"), pullRequest, apiDefinition, validationResult)
            return
        }

        storeResultAndUpdateStatus(
                RequestStatus.success( "API passed all checks ${validationResult.violationsCount}"), pullRequest, apiDefinition, validationResult)
    }

    private fun storeResultAndUpdateStatus(status: RequestStatus,
                                           pullRequest: PullRequest,
                                           apiDefinition: String?,
                                           reviewResponse: ApiDefinitionResponse?) {
        storeValidationResults(pullRequest, apiDefinition, reviewResponse)
        pullRequest.updateCommitState(status.commitState, "https://127.0.0.1", status.description)
    }

    private fun storeValidationResults(pullRequest: PullRequest, apiDefinitionString: String?, validationResult: ApiDefinitionResponse?) {
        validationRepository.save(
                Validation().apply {
                    repositoryUrl = pullRequest.getRepositoryUrl()
                    apiDefinition = apiDefinitionString
                    violations = jsonObjectMapper.writeValueAsString(validationResult)
                })
    }

    data class RequestStatus(val commitState: GHCommitState, val description: String) {
        companion object {
            fun success(description: String) = RequestStatus(SUCCESS, description)
            fun error(description: String) = RequestStatus(ERROR, description)
        }
    }

}