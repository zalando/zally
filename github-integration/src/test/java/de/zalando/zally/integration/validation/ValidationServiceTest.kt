package de.zalando.zally.integration.validation

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.given
import com.nhaarman.mockito_kotlin.then
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
import org.mockito.Matchers
import org.mockito.Mock
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
        validationService = ValidationService(githubService, zallyService, validationRepository, jsonObjectMapper, "http://bark:5000")
        given(validationRepository.save(any<PullRequestValidation>())).willReturn(PullRequestValidation(id = 10))
        given(githubService.parsePayload("", "")).willReturn(pullRequest)
    }

    @Test
    fun shouldSaveResultIfNoConfigurationFile() {
        given(pullRequest.getConfiguration()).willReturn(Optional.empty())

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "http://bark:5000/reports/10", "Could not find zally configuration file")
        then(validationRepository).should().save(any<PullRequestValidation>())
    }

    @Test
    fun shouldSaveResultIfNoApiDefinitionFile() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(simpleConfiguration()))
        given(pullRequest.getApiDefinitions()).willReturn(mapOf("foo.yaml" to Optional.empty()))

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "http://bark:5000/reports/10", "Could not find swagger file")
        then(validationRepository).should().save(any<PullRequestValidation>())
    }

    @Test
    fun shouldSaveResultIfValidationIsSuccessful() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(simpleConfiguration()))
        given(pullRequest.getApiDefinitions()).willReturn(hashMapOf("some-api.yaml" to Optional.of("api-definition-content")))
        given(zallyService.validate(Matchers.anyString())).willReturn(okApiResponse())
        given(pullRequest.isAPIChanged()).willReturn(true)

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.SUCCESS, "http://bark:5000/reports/10", "API Review: 0 MUST, 1 SHOULD, 0 MAY, 0 HINT violations found")
        then(validationRepository).should().save(any<PullRequestValidation>())
    }

    @Test
    fun shouldSaveResultIfValidationIsNotSuccessful() {
        given(pullRequest.getConfiguration()).willReturn(Optional.of(simpleConfiguration()))
        given(pullRequest.getApiDefinitions()).willReturn(hashMapOf("some-api.yaml" to Optional.of("api-definition-content")))
        given(zallyService.validate(Matchers.anyString())).willReturn(badApiResponse())
        given(pullRequest.isAPIChanged()).willReturn(true)

        validationService.validatePullRequest("", "")

        then(pullRequest).should().updateCommitState(GHCommitState.ERROR, "http://bark:5000/reports/10", "API Review: 1 MUST, 0 SHOULD, 0 MAY, 0 HINT violations found")
        then(validationRepository).should().save(any<PullRequestValidation>())
    }

    private fun okApiResponse() = ApiDefinitionResponse(
        violations = listOf(Violation("v-1", "vd-1", ViolationType.SHOULD, "rl-1", listOf("path-1"))),
        violationsCount = mapOf("should" to 1)
    )

    private fun badApiResponse() = ApiDefinitionResponse(
        violations = listOf(Violation("v-2", "vd-2", ViolationType.MUST, "rl-2", listOf("path-2"))),
        violationsCount = mapOf("must" to 1)
    )

    private fun simpleConfiguration() = Configuration().apply {
        this.apiDefinitions = listOf("foo/bar.yaml")
    }
}