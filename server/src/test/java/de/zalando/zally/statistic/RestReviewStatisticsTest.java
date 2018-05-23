package de.zalando.zally.statistic;

import de.zalando.zally.apireview.ApiReview;
import de.zalando.zally.apireview.RestApiBaseTest;
import de.zalando.zally.dto.ApiDefinitionRequest;
import de.zalando.zally.rule.api.Severity;
import de.zalando.zally.rule.Result;
import de.zalando.zally.rule.api.Rule;
import de.zalando.zally.rule.zalando.AvoidTrailingSlashesRule;
import de.zalando.zally.rule.zalando.ZalandoRuleSet;
import de.zalando.zally.util.ErrorResponse;
import de.zalando.zally.util.TestDateUtil;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static de.zalando.zally.util.TestDateUtil.now;
import static de.zalando.zally.util.TestDateUtil.tomorrow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class RestReviewStatisticsTest extends RestApiBaseTest {

    @Test
    public void shouldReturnEmptyReviewStatisticsList() {
        assertThat(getReviewStatistics().getReviews()).isEmpty();
    }

    @Test
    public void shouldReturnAllReviewStatisticsFromLastWeekIfNoIntervalParametersAreSupplied() {
        LocalDate from = now().minusDays(7L).toLocalDate();

        List<ApiReview> reviews = createRandomReviewsInBetween(from, now().toLocalDate());

        ReviewStatistics response = getReviewStatistics();

        assertThat(response.getReviews()).hasSize(reviews.size());
        assertThat(response.getViolations()).hasSize(1);
        assertThat(response.getViolations().get(0).getOccurrence()).isEqualTo(1);
    }

    @Test
    public void shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromParameterTilNow() {
        LocalDate from = now().minusDays(5L).toLocalDate();

        // this data should not be loaded later
        createRandomReviewsInBetween(from.minusDays(10L), from.minusDays(5L));

        List<ApiReview> reviews = createRandomReviewsInBetween(from, now().toLocalDate());

        ReviewStatistics response = getReviewStatisticsBetween(from, null);

        assertThat(response.getNumberOfEndpoints()).isEqualTo(reviews.size() * 2);
        assertThat(response.getMustViolations()).isEqualTo(reviews.size());
        assertThat(response.getTotalReviews()).isEqualTo(reviews.size());
        assertThat(response.getSuccessfulReviews()).isEqualTo(reviews.size());
        assertThat(response.getReviews()).hasSize(reviews.size());
        assertThat(response.getViolations()).hasSize(1);
    }

    @Test
    public void shouldReturnAllReviewStatisticsFromIntervalSpecifiedByFromAndToParameters() {
        LocalDate from = now().minusDays(5L).toLocalDate();
        LocalDate to = TestDateUtil.yesterday().minusDays(1L).toLocalDate();

        List<ApiReview> reviews = createRandomReviewsInBetween(from, now().toLocalDate());

        ReviewStatistics response = getReviewStatisticsBetween(from, to);
        assertThat(response.getReviews()).hasSize(reviews.size() - 1);
    }

    @Test
    public void shouldReturnBadRequestForFromInTheFuture() {
        assertBadRequestFor(tomorrow().toLocalDate(), null);
    }

    @Test
    public void shouldReturnBadRequestWhenToParameterIsProvidedWithoutFromParameter() {
        assertBadRequestFor(null, tomorrow().toLocalDate());
    }

    @Test
    public void shouldReturnBadRequestForMalformedFromParameter() {
        assertBadRequestFor("nodate", null);
    }

    @Test
    public void shouldReturnBadRequestForMalformedToParameter() {
        assertBadRequestFor(null, "nodate");
    }

    @Test
    public void shouldReturnApiName() {
        int reviewsCount = 7;
        createRandomReviewsInBetween(now().minusDays(reviewsCount).toLocalDate(), now().toLocalDate());
        ReviewStatistics response = getReviewStatistics();
        assertThat(response.getReviews()).hasSize(reviewsCount);
        assertThat(response.getReviews().get(0).getApi()).isEqualTo("My API");
    }

    @Test
    public void shouldReturnNumberOfUniqueApiReviewsBasedOnApiName() {
        LocalDate now = now().toLocalDate();
        apiReviewRepository.save(apiReview(now, "API A", null));
        apiReviewRepository.save(apiReview(now, "API B", null));
        apiReviewRepository.save(apiReview(now, "API B", null));

        ReviewStatistics statistics = getReviewStatistics();

        assertThat(statistics.getReviews()).hasSize(3);
        assertThat(statistics.getTotalReviewsDeduplicated()).isEqualTo(2);
    }

    @Test
    public void deduplicatedReviewStatisticsShouldIgnoreApisWithoutName() {
        LocalDate now = now().toLocalDate();
        apiReviewRepository.save(apiReview(now, null, null));
        apiReviewRepository.save(apiReview(now, "", null));
        apiReviewRepository.save(apiReview(now, "Nice API", null));

        ReviewStatistics statistics = getReviewStatistics();

        assertThat(statistics.getReviews()).hasSize(3);
        assertThat(statistics.getTotalReviewsDeduplicated()).isEqualTo(1);
    }

    @Test
    public void shouldStoreUserAgent() {
        LocalDate now = now().toLocalDate();
        apiReviewRepository.save(apiReview(now, null, "curl"));

        ReviewStatistics statistics = getReviewStatistics();
        assertThat(statistics.getReviews()).hasSize(1);
        assertThat(statistics.getReviews().get(0).getUserAgent()).isEqualTo("curl");
    }

    @Test
    public void shouldFilterByUserAgent() {
        LocalDate now = now().toLocalDate();
        apiReviewRepository.save(apiReview(now, null, "curl"));
        apiReviewRepository.save(apiReview(now, null, null));

        ReviewStatistics statistics = getReviewStatistics();
        assertThat(statistics.getReviews()).hasSize(2);

        statistics = getReviewStatisticsByUserAgent("curl");
        assertThat(statistics.getReviews()).hasSize(1);
    }

    private List<ApiReview> createRandomReviewsInBetween(LocalDate from, LocalDate to) {
        List<ApiReview> reviews = new LinkedList<>();

        LocalDate currentDate = LocalDate.from(from);
        while (currentDate.isBefore(to)) {
            reviews.add(apiReview(currentDate, "My API", null));
            currentDate = currentDate.plusDays(1L);
        }

        apiReviewRepository.saveAll(reviews);
        return reviews;
    }

    private ApiReview apiReview(LocalDate date, String apiName, String userAgent) {
        ApiReview review = new ApiReview(new ApiDefinitionRequest(), null, "dummyApiDefinition", createRandomViolations());
        review.setDay(date);
        review.setName(apiName);
        review.setNumberOfEndpoints(2);
        review.setUserAgent(userAgent);

        return review;
    }

    private List<Result> createRandomViolations() {
        return Arrays.asList(new Result(new ZalandoRuleSet(), AvoidTrailingSlashesRule.class.getAnnotation(Rule.class), "", Severity.MUST, "#/pointer"));
    }

    private void assertBadRequestFor(Object from, Object to) {
        ResponseEntity<ErrorResponse> response = getReviewStatisticsBetween(from, to, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo(APPLICATION_PROBLEM_JSON);
        assertThat(response.getBody().getTitle()).isEqualTo(BAD_REQUEST.getReasonPhrase());
        assertThat(response.getBody().getStatus()).isNotEmpty();
        assertThat(response.getBody().getDetail()).isNotEmpty();
    }
}
