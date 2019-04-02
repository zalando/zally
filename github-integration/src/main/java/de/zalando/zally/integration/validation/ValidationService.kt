package de.zalando.zally.integration.validation

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.GithubService
import de.zalando.zally.integration.github.PullRequest
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import de.zalando.zally.integration.zally.ViolationType
import de.zalando.zally.integration.zally.ZallyService
import org.kohsuke.github.GHCommitState
import org.kohsuke.github.GHCommitState.ERROR
import org.kohsuke.github.GHCommitState.FAILURE
import org.kohsuke.github.GHCommitState.PENDING
import org.kohsuke.github.GHCommitState.SUCCESS
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class ValidationService(
    private val githubService: GithubService,
    private val zallyService: ZallyService,
    private val validationRepository: ValidationRepository,
    private val jsonObjectMapper: ObjectMapper,
    @Value("\${bark.serverUrl}") private val serverUrl: String
) {

    fun validatePullRequest(payload: String, signature: String) {

        val pullRequest = githubService.parsePayload(payload, signature)
        try {

            val pendingState = RequestStatus.pending("Validation is in progress.")
            pullRequest.updateCommitState(pendingState.commitState, null, pendingState.description)

            val configuration = pullRequest.getConfiguration()

            if (!configuration.isPresent) {
                storeResultAndUpdateStatus(
                    RequestStatus.error("Could not find zally configuration file"), pullRequest, null)
                return
            }

            val apiDefinitions = pullRequest.getApiDefinitions()
            if (!apiDefinitions.all {
                    it.value.isPresent
                }) {
                storeResultAndUpdateStatus(
                    RequestStatus.error("Could not find swagger file"), pullRequest, null)
                return
            }

            if (!pullRequest.isAPIChanged()) {
                storeResultAndUpdateStatus(
                    RequestStatus.success("No API changes detected"), pullRequest, null)
                return
            }

            val validationResults = HashMap<String, ApiValidationResult>()

            for ((fileName, apiDefinition) in apiDefinitions) {
                apiDefinition.ifPresent {
                    val validationResult = zallyService.validate(it)
                    validationResults.put(fileName, ApiValidationResult(it, validationResult))
                }
            }

            val validationResult = ViolationType.values().map {
                it to 0
            }.toMap().toMutableMap()

            validationResults.flatMap {
                it.value.result.violations
            }.forEach {
                val violationType = it.violationType
                if (violationType != null) {
                    validationResult[violationType] = validationResult.getValue(violationType) + 1
                }
            }

            val invalid = validationResult.getOrDefault(ViolationType.MUST, 0) > 0

            if (invalid) {
                storeResultAndUpdateStatus(
                    RequestStatus.error("API Review: ${validationResult[ViolationType.MUST]} MUST, " +
                        "${validationResult[ViolationType.SHOULD]} SHOULD, " +
                        "${validationResult[ViolationType.MAY]} MAY, " +
                        "${validationResult[ViolationType.HINT]} HINT violations found"),
                    pullRequest,
                    validationResults
                )
                return
            }

            val hasErrors = validationResult.values.sum() > 0

            if (hasErrors) {
                storeResultAndUpdateStatus(
                    RequestStatus.success("API Review: ${validationResult[ViolationType.MUST]} MUST, " +
                        "${validationResult[ViolationType.SHOULD]} SHOULD, " +
                        "${validationResult[ViolationType.MAY]} MAY, " +
                        "${validationResult[ViolationType.HINT]} HINT violations found"),
                    pullRequest,
                    validationResults
                )
                return
            }

            storeResultAndUpdateStatus(
                RequestStatus.success("API Review: No violations :+1:"),
                pullRequest,
                validationResults
            )
        } catch (e: Exception) {
            val errorRequestState = RequestStatus.failure("Failed with internal server error")
            pullRequest.updateCommitState(errorRequestState.commitState, null, errorRequestState.description)
            throw e
        }
    }

    private fun storeResultAndUpdateStatus(
        status: RequestStatus,
        pullRequest: PullRequest,
        reviewResponse: Map<String, ApiValidationResult>?
    ) {
        val validation = storeValidationResults(pullRequest, reviewResponse)
        pullRequest.updateCommitState(status.commitState, reportLink(validation.id), status.description)
    }

    private fun reportLink(id: Long?): String = "$serverUrl/reports/$id"

    private fun storeValidationResults(pullRequest: PullRequest, validationResults: Map<String, ApiValidationResult>?): PullRequestValidation =
        validationRepository.save(
            PullRequestValidation().apply {
                pullRequestInfo = jsonObjectMapper.writeValueAsString(pullRequest.eventInfo)
                val that = this
                apiValidations = validationResults?.map {
                    ApiValidation(
                        fileName = it.key,
                        violations = jsonObjectMapper.writeValueAsString(it.value.result),
                        apiDefinition = it.value.definition,
                        pullRequestValidation = that
                    )
                }.orEmpty()
            })

    data class RequestStatus(val commitState: GHCommitState, val description: String) {
        companion object {
            fun success(description: String) = RequestStatus(SUCCESS, description)
            fun error(description: String) = RequestStatus(ERROR, description)
            fun failure(description: String) = RequestStatus(FAILURE, description)
            fun pending(description: String) = RequestStatus(PENDING, description)
        }
    }

    data class ApiValidationResult(val definition: String, val result: ApiDefinitionResponse)
}