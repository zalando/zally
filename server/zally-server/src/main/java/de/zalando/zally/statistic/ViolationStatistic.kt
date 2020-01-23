package de.zalando.zally.statistic

import de.zalando.zally.apireview.RuleViolation
import de.zalando.zally.rule.api.Severity

class ViolationStatistic {

    var name: String? = null
    var type: Severity? = null
    var occurrence: Int = 0

    internal constructor() {}

    internal constructor(violation: RuleViolation, occurrence: Int) {
        this.name = violation.name
        this.type = violation.type
        this.occurrence = occurrence
    }
}
