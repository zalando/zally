package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReview
import de.zalando.zally.apireview.RestApiBaseTest
import de.zalando.zally.dto.ApiDefinitionRequest
import de.zalando.zally.rule.Result
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.zalando.AvoidTrailingSlashesRule
import de.zalando.zally.rule.zalando.ZalandoRuleSet
import de.zalando.zally.util.ErrorResponse
import de.zalando.zally.util.TestDateUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.springframework.http.HttpStatus.BAD_REQUEST
import java.time.LocalDate

class RestReviewStatisticsTest : RestApiBaseTest() {

    @Test
    fun shouldReturnEmptyReviewStatisticsList() {
        assertThat(reviewStatistics.reviews).isEmpty()
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromLastWeekIfNoIntervalParametersAreSupplied() {
        val from = TestDateUtil.now().minusDays(7L).toLocalDate()

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = reviewStatistics

        assertThat(response.reviews).hasSize(reviews.size)
        assertThat(response.violations).hasSize(1)
        assertThat(response.violations!![0].occurrence).isEqualTo(1)
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromParameterTilNow() {
        val from = TestDateUtil.now().minusDays(5L).toLocalDate()

        // this data should not be loaded later
        createRandomReviewsInBetween(from.minusDays(10L), from.minusDays(5L))

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = getReviewStatisticsBetween(from, null)

        assertThat(response.numberOfEndpoints).isEqualTo(reviews.size * 2)
        assertThat(response.mustViolations).isEqualTo(reviews.size)
        assertThat(response.totalReviews).isEqualTo(reviews.size)
        assertThat(response.successfulReviews).isEqualTo(reviews.size)
        assertThat(response.reviews).hasSize(reviews.size)
        assertThat(response.violations).hasSize(1)
    }

    @Test
    fun shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromAndToParameters() {
        val from = TestDateUtil.now().minusDays(5L).toLocalDate()
        val to = TestDateUtil.yesterday().minusDays(1L).toLocalDate()

        val reviews = createRandomReviewsInBetween(from, TestDateUtil.now().toLocalDate())

        val response = getReviewStatisticsBetween(from, to)
        assertThat(response.reviews).hasSize(reviews.size - 1)
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
    fun shouldReturnApiNameAndId() {
        val reviewsCount = 7
        createRandomReviewsInBetween(TestDateUtil.now().minusDays(reviewsCount.toLong()).toLocalDate(), TestDateUtil.now().toLocalDate())
        val response = reviewStatistics
        assertThat(response.reviews).hasSize(reviewsCount)
        assertThat(response.reviews!![0].api).isEqualTo("My API")
        assertThat(response.reviews!![0].apiId).isEqualTo("48aa0090-25ef-11e8-b467-0ed5f89f718b")
    }

    @Test
    fun shouldReturnNumberOfUniqueApiReviewsBasedOnApiName() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository!!.save(apiReview(now, "API A", null))
        apiReviewRepository!!.save(apiReview(now, "API B", null))
        apiReviewRepository!!.save(apiReview(now, "API B", null))

        val statistics = reviewStatistics

        assertThat(statistics.reviews).hasSize(3)
        assertThat(statistics.totalReviewsDeduplicated).isEqualTo(2)
    }

    @Test
    fun deduplicatedReviewStatisticsShouldIgnoreApisWithoutName() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository!!.save(apiReview(now, null, null))
        apiReviewRepository!!.save(apiReview(now, "", null))
        apiReviewRepository!!.save(apiReview(now, "Nice API", null))

        val statistics = reviewStatistics

        assertThat(statistics.reviews).hasSize(3)
        assertThat(statistics.totalReviewsDeduplicated).isEqualTo(1)
    }

    @Test
    fun shouldStoreUserAgent() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository!!.save(apiReview(now, null, "curl"))

        val statistics = reviewStatistics
        assertThat(statistics.reviews).hasSize(1)
        assertThat(statistics.reviews!![0].userAgent).isEqualTo("curl")
    }

    @Test
    fun shouldFilterByUserAgent() {
        val now = TestDateUtil.now().toLocalDate()
        apiReviewRepository!!.save(apiReview(now, null, "curl"))
        apiReviewRepository!!.save(apiReview(now, null, null))

        var statistics = reviewStatistics
        assertThat(statistics.reviews).hasSize(2)

        statistics = getReviewStatisticsByUserAgent("curl")
        assertThat(statistics.reviews).hasSize(1)
    }

    private fun createRandomReviewsInBetween(from: LocalDate, to: LocalDate): List<ApiReview> {
        val reviews = mutableListOf<ApiReview>()

        var currentDate = LocalDate.from(from)
        while (currentDate.isBefore(to)) {
            reviews.add(apiReview(currentDate, "My API", null))
            currentDate = currentDate.plusDays(1L)
        }

        apiReviewRepository!!.saveAll(reviews)
        return reviews
    }

    private fun apiReview(date: LocalDate, apiName: String?, userAgent: String?): ApiReview {
        val review = ApiReview(ApiDefinitionRequest(), null, "dummyApiDefinition", createRandomViolations())
        review.day = date
        review.name = apiName
        review.apiId = "48aa0090-25ef-11e8-b467-0ed5f89f718b"
        review.numberOfEndpoints = 2
        review.userAgent = userAgent

        return review
    }

    private fun createRandomViolations(): List<Result> {
        return listOf(Result(ZalandoRuleSet(), AvoidTrailingSlashesRule::class.java.getAnnotation(Rule::class.java), "", Severity.MUST, "/pointer"))
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
