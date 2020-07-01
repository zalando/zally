package org.zalando.zally.statistic

import org.zalando.zally.apireview.ApiReview

data class ReviewStatisticsByName(
    val name: String,
    val numberOfEndpoints: Long = 0,
    val mustViolations: Long = 0,
    val shouldViolations: Long = 0,
    val mayViolations: Long = 0,
    val hintViolations: Long = 0
) {
    companion object {
        fun of(apiReview: ApiReview): ReviewStatisticsByName {
            return ReviewStatisticsByName(
                apiReview.name!!,
                apiReview.numberOfEndpoints.toLong(),
                apiReview.mustViolations.toLong(),
                apiReview.shouldViolations.toLong(),
                apiReview.mayViolations.toLong(),
                apiReview.hintViolations.toLong()
            )
        }
    }
}
