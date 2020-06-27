package org.zalando.zally.statistic

data class ReviewStatistics(
    val totalReviews: Long = 0,
    val totalReviewsDeduplicated: Long = 0,
    val successfulReviews: Long = 0,
    val numberOfEndpoints: Long = 0,
    val mustViolations: Long = 0,
    val shouldViolations: Long = 0,
    val mayViolations: Long = 0,
    val hintViolations: Long = 0
)
