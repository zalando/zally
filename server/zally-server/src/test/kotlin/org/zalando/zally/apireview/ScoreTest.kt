package org.zalando.zally.apireview

import com.fasterxml.jackson.core.JsonPointer
import org.assertj.core.api.Assertions
import org.assertj.core.util.Lists
import org.junit.Test
import org.zalando.zally.core.Result
import org.zalando.zally.dto.ApiDefinitionRequest
import org.zalando.zally.rule.api.Severity
import java.net.URI
import java.time.LocalDate
import java.time.OffsetDateTime

class ScoreTest {

    @Test
    fun shouldReturnExpectedScore() {
        val score = Score.forLinterResult(linterResultForTesting(3, 2, 1))
        Assertions.assertThat(score).isEqualTo(0.29f)
    }

    @Test
    fun shouldReturnMinimalScoreOverall() {
        val score = Score.forLinterResult(linterResultForTesting(10, 10, 10))
        Assertions.assertThat(score).isEqualTo(0.0f)
    }

    @Test
    fun shouldReturnMinimalScoreWithoutMustViolations() {
        val score = Score.forLinterResult(linterResultForTesting(0, 10, 10))
        Assertions.assertThat(score).isEqualTo(0.8f)
    }

    private fun linterResultForTesting(numberOfMusts: Int, numberOfShoulds: Int, numberOfMays: Int): ApiReview {
        return ApiReview(
            ApiDefinitionRequest(),
            "userAgent",
            "apiDefinition",
            violationResultsForTesting(numberOfMusts, numberOfShoulds, numberOfMays),
            "name",
            "apiId",
            OffsetDateTime.now(),
            LocalDate.now())
    }

    private fun violationResultsForTesting(numberOfMusts: Int, numberOfShoulds: Int, numberOfMays: Int): List<Result> {
        return Lists.emptyList<Result>()
            .plus(generateResultsForTesting(Severity.MUST, numberOfMusts))
            .plus(generateResultsForTesting(Severity.SHOULD, numberOfShoulds))
            .plus(generateResultsForTesting(Severity.MAY, numberOfMays))
    }

    private fun generateResultsForTesting(severity: Severity, numberOfDistinctResults: Int): List<Result> {
        return generateSequence(1) { it + 1 }
            .take(numberOfDistinctResults)
            .map {
                Result(
                    "SomeID",
                    URI.create("http://zalando.de"),
                    "Title with ID $it",
                    "Some Arbitrary Description for ID $it",
                    severity,
                    JsonPointer.empty(),
                    IntRange.EMPTY)
            }
            .toList()
    }
}
