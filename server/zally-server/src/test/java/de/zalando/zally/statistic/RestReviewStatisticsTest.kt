package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReview
import de.zalando.zally.apireview.RestApiBaseTest
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.dto.ApiDefinitionRequest
import de.zalando.zally.core.Result
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.util.ErrorResponse
import de.zalando.zally.util.TestDateUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.net.URI
import java.time.LocalDate

class RestReviewStatisticsTest : RestApiBaseTest() {

    @Test
    fun shouldReturnEmptyReviewStatisticsList() {
        assertThat(reviewStatistics.totalReviews).isEqualTo(0)
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromLastWeekIfNoIntervalParametersAreSupplied() {
        val from = TestDateUtil.now().minusDays(7L).toLocalDate()

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = reviewStatistics

        assertThat(response.totalReviews.toInt()).isEqualTo(reviews.size)
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromParameterTilNow() {
        val from = TestDateUtil.now().minusDays(5L).toLocalDate()

        // this data should not be loaded later
        createRandomReviewsInBetween(from.minusDays(10L), from.minusDays(5L))

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = getReviewStatisticsBetween(from, null)

        assertThat(response.numberOfEndpoints.toInt()).isEqualTo(reviews.size * 2)
        assertThat(response.mustViolations.toInt()).isEqualTo(reviews.size)
        assertThat(response.totalReviews.toInt()).isEqualTo(reviews.size)
        assertThat(response.successfulReviews.toInt()).isEqualTo(reviews.size)
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromAndToParameters() {
        val from = TestDateUtil.now().minusDays(5L).toLocalDate()
        val to = TestDateUtil.yesterday().minusDays(1L).toLocalDate()

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = getReviewStatisticsBetween(from, to)
        assertThat(response.totalReviews.toInt()).isEqualTo(reviews.size - 1)
    }

    @Test
    fun shouldReturnBadRequestForFromInTheFuture() {
        assertBadRequestFor(TestDateUtil.tomorrow().toLocalDate(), null)
    }

    @Test
    fun shouldReturnBadRequestWhenToParameterIsProvidedWithoutFromParameter() {
        assertBadRequestFor(null, TestDateUtil.tomorrow().toLocalDate())
    }

    @Test
    fun shouldReturnBadRequestForMalformedFromParameter() {
        assertBadRequestFor("nodate", null)
    }

    @Test
    fun shouldReturnBadRequestForMalformedToParameter() {
        assertBadRequestFor(null, "nodate")
    }

    @Test
    fun shouldReturnNumberOfUniqueApiReviewsBasedOnApiName() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository.save(apiReview(now, "API A", ""))
        apiReviewRepository.save(apiReview(now, "API B", ""))
        apiReviewRepository.save(apiReview(now, "API B", ""))

        val statistics = reviewStatistics

        assertThat(statistics.totalReviews).isEqualTo(3)
        assertThat(statistics.totalReviewsDeduplicated).isEqualTo(2)
    }

    @Test
    fun shouldFilterByUserAgent() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository.save(apiReview(now, null, "curl"))
        apiReviewRepository.save(apiReview(now, null, ""))

        var statistics = reviewStatistics
        assertThat(statistics.totalReviews).isEqualTo(2)

        statistics = getReviewStatisticsByUserAgent("curl")
        assertThat(statistics.totalReviews).isEqualTo(1)
    }

    private fun createRandomReviewsInBetween(from: LocalDate, to: LocalDate): List<ApiReview> {
        val reviews = mutableListOf<ApiReview>()

        var currentDate = LocalDate.from(from)
        while (currentDate.isBefore(to)) {
            reviews.add(apiReview(currentDate, "My API", ""))
            currentDate = currentDate.plusDays(1L)
        }

        apiReviewRepository.saveAll(reviews)
        return reviews
    }

    private fun apiReview(date: LocalDate, apiName: String?, userAgent: String): ApiReview {
        return ApiReview(
            request = ApiDefinitionRequest(),
            userAgent = userAgent,
            apiDefinition = "dummyApiDefinition",
            violations = createRandomViolations(),
            day = date,
            name = apiName,
            apiId = "48aa0090-25ef-11e8-b467-0ed5f89f718b",
            numberOfEndpoints = 2
        )
    }

    private fun createRandomViolations(): List<Result> {
        return listOf(
            Result(
                id = "TestRuleId",
                url = URI.create("http://rules.example.com/test"),
                title = "Test Rule Title",
                description = "Description of test rule",
                violationType = Severity.MUST,
                pointer = "/pointer".toJsonPointer()
            )
        )
    }

    private fun assertBadRequestFor(from: Any?, to: Any?) {
        val response = getReviewStatisticsBetween(from, to, ErrorResponse::class.java)

        assertThat(response.statusCode).isEqualTo(BAD_REQUEST)
        assertThat(response.headers.contentType!!.toString()).isEqualTo(RestApiBaseTest.APPLICATION_PROBLEM_JSON)
        assertThat(response.body!!.title).isEqualTo(BAD_REQUEST.reasonPhrase)
        assertThat(response.body!!.status).isNotEmpty()
        assertThat(response.body!!.detail).isNotEmpty()
    }
}
