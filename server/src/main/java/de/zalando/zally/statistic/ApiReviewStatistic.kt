package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReview
import de.zalando.zally.apireview.RuleViolation

import java.util.LinkedList

class ApiReviewStatistic {

    var api: String? = null
    var apiId: String? = null
    var isSuccessful: Boolean = false
    var numberOfEndpoints: Int = 0
    var userAgent: String? = null
    var violations: List<RuleViolation>? = null

    internal constructor() {}

    internal constructor(apiReview: ApiReview) {
        api = apiReview.name
        apiId = apiReview.apiId
        isSuccessful = apiReview.isSuccessfulProcessed
        numberOfEndpoints = apiReview.numberOfEndpoints
        userAgent = apiReview.userAgent
        violations = LinkedList(apiReview.ruleViolations)
    }
}
