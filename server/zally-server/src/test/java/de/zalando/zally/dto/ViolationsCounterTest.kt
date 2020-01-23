package de.zalando.zally.dto

import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.core.Result
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Severity.HINT
import de.zalando.zally.rule.api.Severity.MAY
import de.zalando.zally.rule.api.Severity.MUST
import de.zalando.zally.rule.api.Severity.SHOULD
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URI

class ViolationsCounterTest {

    @Test
    fun returnsZerosWhenViolationListIsEmpty() {
        val results = emptyList<Result>()
        val counts = hashMapOf(
            MUST to 0,
            SHOULD to 0,
            MAY to 0,
            HINT to 0
        )

        assertCounters(counts, results)
    }

    @Test
    fun countsMustViolations() {
        val results = listOfResults(5, MUST)
        val counts = hashMapOf(
            MUST to 5,
            SHOULD to 0,
            MAY to 0,
            HINT to 0
        )

        assertCounters(counts, results)
    }

    @Test
    fun countsShouldViolations() {
        val results = listOfResults(4, SHOULD)
        val counts = hashMapOf(
            MUST to 0,
            SHOULD to 4,
            MAY to 0,
            HINT to 0
        )

        assertCounters(counts, results)
    }

    @Test
    fun countsMayViolations() {
        val results = listOfResults(3, MAY)
        val counts = hashMapOf(
            MUST to 0,
            SHOULD to 0,
            MAY to 3,
            HINT to 0
        )

        assertCounters(counts, results)
    }

    @Test
    fun countsHintViolations() {
        val results = listOfResults(2, HINT)
        val counts = hashMapOf(
            MUST to 0,
            SHOULD to 0,
            MAY to 0,
            HINT to 2
        )

        assertCounters(counts, results)
    }

    @Test
    fun countsMixedViolations() {
        val results =
            listOfResults(1, MUST) +
                listOfResults(2, SHOULD) +
                listOfResults(5, HINT)
        val counts = hashMapOf(
            MUST to 1,
            SHOULD to 2,
            MAY to 0,
            HINT to 5
        )

        assertCounters(counts, results)
    }

    private fun listOfResults(count: Int, severity: Severity): List<Result> {
        return List(count) {
            Result(
                id = "TestRuleId",
                url = URI.create("http://rules.example.com/test"),
                title = "Test Rule Title",
                description = "Description of test rule",
                violationType = severity,
                pointer = "/pointer".toJsonPointer()
            )
        }
    }

    private fun assertCounters(expected: Map<Severity, Int>, results: List<Result>) {
        val counter = ViolationsCounter(results)
        for (violationType in expected.keys) {
            assertEquals(expected[violationType], counter.getCounter(violationType))
        }
    }
}
