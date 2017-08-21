package de.zalando.zally.integration.validation

import com.fasterxml.jackson.databind.ObjectMapper
import de.zalando.zally.integration.github.GithubService
import de.zalando.zally.integration.github.PullRequest
import de.zalando.zally.integration.zally.ApiDefinitionResponse
import de.zalando.zally.integration.zally.Configuration
import de.zalando.zally.integration.zally.Violation
import de.zalando.zally.integration.zally.ViolationType
import de.zalando.zally.integration.zally.ZallyService
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kohsuke.github.GHCommitState
import org.mockito.Mock
import org.mockito.BDDMockito
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Matchers
import org.mockito.runners.MockitoJUnitRunner
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class ValidationServiceTest {

    @Mock lateinit var githubService: GithubService
    @Mock lateinit var zallyService: ZallyService
    @Mock lateinit var validationRepository: ValidationRepository
    @Mock lateinit var pullRequest: PullRequest

    lateinit var jsonObjectMapper: ObjectMapper
    lateinit var validationService: ValidationService

    @Before
    fun setUp() {
        jsonObjectMapper = ObjectMapper()
        validationService = ValidationService(githubService, zallyService, validationRepository, jsonObjectMapper)
        given(githubService.parsePayload("", "")).willReturn(pullRequest)
    }

    @Test
    fun shouldSaveResultIfNoConfigurationFile() {
        given(pullRequest.getConfiguration()).willReturn(Optional.empty())

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Could not find zally configuration file")
        then(validationRepository).should().save(any<Validation>())
    }

    @Test
    fun shouldSaveResultIfNoApiDefinitionFile() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(Configuration()))
        given(pullRequest.getSwaggerFile()).willReturn(Optional.empty())

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Could not find swagger file")
        then(validationRepository).should().save(any<Validation>())
    }

    @Test
    fun shouldSaveResultIfValidationIsSuccessful() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(Configuration()))
        given(pullRequest.getSwaggerFile()).willReturn(Optional.of("api-definition-content"))
        given(zallyService.validate(Matchers.anyString())).willReturn(okApiResponse())

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.SUCCESS, "https://127.0.0.1", "API passed all checks {should=1}")
        then(validationRepository).should().save(any<Validation>())
    }

    @Test
    fun shouldSaveResultIfValidationIsNotSuccessful() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(Configuration()))
        given(pullRequest.getSwaggerFile()).willReturn(Optional.of("api-definition-content"))
        given(zallyService.validate(Matchers.anyString())).willReturn(badApiResponse())

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "https://127.0.0.1", "Got violations")
        then(validationRepository).should().save(any<Validation>())
    }

    private fun okApiResponse() = ApiDefinitionResponse().apply {
        violations = listOf(Violation("v-1", "vd-1", ViolationType.SHOULD, "rl-1", listOf("path-1")))
        violationsCount = mapOf("should" to 1)
    }

    private fun badApiResponse() = ApiDefinitionResponse().apply {
        violations = listOf(Violation("v-2", "vd-2", ViolationType.MUST, "rl-2", listOf("path-2")))
        violationsCount = mapOf("must" to 1)
    }

    private fun <T> any(): T {
        BDDMockito.any<T>()
        return null as T
    }

}