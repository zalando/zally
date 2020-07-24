package org.zalando.zally.apireview

import org.zalando.zally.Application
import org.zalando.zally.apireview.RestApiTestConfiguration.Companion.assertRuleManagerUsingLimitedRules
import org.zalando.zally.core.RulesManager
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.dto.ApiDefinitionResponse
import org.zalando.zally.dto.RuleDTO
import org.zalando.zally.dto.RulesListDTO
import org.zalando.zally.statistic.ReviewStatistics
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.util.UriComponentsBuilder.fromPath
import java.time.LocalDate

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = [Application::class, RestApiTestConfiguration::class]
)
@ActiveProfiles("test", "limited-rules")
abstract class RestApiBaseTest {

    @Autowired
    protected lateinit var restTemplate: TestRestTemplate

    @Autowired
    protected lateinit var apiReviewRepository: ApiReviewRepository

    @Autowired
    private lateinit var rules: RulesManager

    @Test
    fun `correct rules are under test`() = assertRuleManagerUsingLimitedRules(rules)

    protected val reviewStatistics: ReviewStatistics
        get() = getReviewStatisticsBetween(null, null)

    protected val supportedRules: List<RuleDTO>
        get() = getSupportedRules(null, null)

    @Before
    fun cleanDatabase() {
        apiReviewRepository.deleteAll()
    }

    protected fun <T> sendApiDefinition(request: ApiDefinitionRequest, responseType: Class<T>): ResponseEntity<T> =
        restTemplate.postForEntity(API_VIOLATIONS_URL, request, responseType)

    protected fun sendApiDefinition(request: ApiDefinitionRequest): ApiDefinitionResponse {
        val responseEntity = sendApiDefinition(request, ApiDefinitionResponse::class.java)

        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        return responseEntity.body!!
    }

    protected fun <T> getReviewStatisticsBetween(from: Any?, to: Any?, responseType: Class<T>): ResponseEntity<T> {
        val url = fromPath(REVIEW_STATISTICS_URL)
            .queryParam("from", from)
            .queryParam("to", to)
            .build()
            .encode()
            .toUriString()

        return restTemplate.getForEntity(url, responseType)
    }

    protected fun <T> getReviewStatisticsByUserAgent(userAgent: Any, responseType: Class<T>): ResponseEntity<T> {
        val url = fromPath(REVIEW_STATISTICS_URL)
            .queryParam("user_agent", userAgent)
            .build()
            .encode()
            .toUriString()

        return restTemplate.getForEntity(url, responseType)
    }

    protected fun getReviewStatisticsBetween(from: LocalDate?, to: LocalDate?): ReviewStatistics {
        val responseEntity = getReviewStatisticsBetween(from, to, ReviewStatistics::class.java)
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        return responseEntity.body!!
    }

    protected fun getReviewStatisticsByUserAgent(userAgent: String): ReviewStatistics {
        val responseEntity = getReviewStatisticsByUserAgent(userAgent, ReviewStatistics::class.java)
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        return responseEntity.body!!
    }

    protected fun getSupportedRules(ruleType: String?, active: Boolean?): List<RuleDTO> {
        val responseEntity = getSupportedRules(ruleType, active, RulesListDTO::class.java)
        assertThat(responseEntity.statusCode).isEqualTo(HttpStatus.OK)
        return responseEntity.body!!.rules
    }

    protected fun <T> getSupportedRules(
        ruleType: String?,
        active: Boolean?,
        responseType: Class<T>
    ): ResponseEntity<T> {
        val url = fromPath(SUPPORTED_RULES_URL)
            .queryParam("type", ruleType)
            .queryParam("is_active", active)
            .build().encode().toUriString()

        return restTemplate.getForEntity(url, responseType)
    }

    companion object {
        const val API_VIOLATIONS_URL = "/api-violations"
        const val REVIEW_STATISTICS_URL = "/review-statistics"
        const val SUPPORTED_RULES_URL = "/supported-rules"

        const val APPLICATION_PROBLEM_JSON = "application/problem+json"
    }
}
