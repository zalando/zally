package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReview

class ReviewStatistics {

    var totalReviews: Int = 0
    var totalReviewsDeduplicated: Int = 0
    var successfulReviews: Int = 0
    var numberOfEndpoints: Int = 0
    var mustViolations: Int = 0
    var shouldViolations: Int = 0
    var mayViolations: Int = 0
    var hintViolations: Int = 0
    var violations: List<ViolationStatistic>? = null
    var reviews: List<ApiReviewStatistic>? = null

    internal constructor() {}

    internal constructor(apiReviews: Collection<ApiReview>) {
        totalReviews = apiReviews.size

        totalReviewsDeduplicated = apiReviews
                .filter { r -> r.name != null && !r.name.isEmpty() }
                .groupBy { it.name }
                .size

        successfulReviews = apiReviews
                .filter { it.isSuccessfulProcessed }
                .size

        numberOfEndpoints = apiReviews
                .map { it.numberOfEndpoints }
                .sum()
        mustViolations = apiReviews
                .map { it.mustViolations }
                .sum()
        shouldViolations = apiReviews
                .map { it.shouldViolations }
                .sum()
        mayViolations = apiReviews
                .map { it.mayViolations }
                .sum()
        hintViolations = apiReviews
                .map { it.hintViolations }
                .sum()
        violations = apiReviews
                .flatMap { it.ruleViolations }
                .groupBy { it.name }
                .filter { !it.value.isEmpty() }
                .map { ViolationStatistic(it.value[0], 1) }
        reviews = apiReviews
                .map { ApiReviewStatistic(it) }
    }
}
