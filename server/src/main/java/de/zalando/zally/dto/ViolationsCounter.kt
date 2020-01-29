package de.zalando.zally.dto

import de.zalando.zally.core.Result
import de.zalando.zally.rule.api.Severity

class ViolationsCounter(violations: List<Result>) {

    private val counters = violations.groupingBy { it.violationType }.eachCount()

    operator fun get(severity: Severity): Int = counters[severity] ?: 0
    fun getCounter(violationType: Severity): Int = counters[violationType] ?: 0
}
