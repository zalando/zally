package org.zalando.zally.statistic

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.zalando.zally.apireview.ApiReviewRepository
import java.util.concurrent.atomic.AtomicLong

@Component
class ReviewMetrics(private val apiReviewRepository: ApiReviewRepository, private val meterRegistry: MeterRegistry) {

    final val statisticsReferences = mutableListOf<Map<String, Map<String, AtomicLong>>>()

    @Value("\${metrics.review.name-prefix:zally_}")
    lateinit var metricsNamePrefix: String

    @Scheduled(fixedDelayString = "\${metrics.review.fixed-delay:300000}", initialDelay = 10000)
    fun updateMetrics() {
        LOG.debug("Updating metrics for review statistics")
        apiReviewRepository.findLatestApiReviews()
            .map(ReviewStatisticsByName.Companion::of)
            .forEach { statistic ->
                statisticsReferences
                    .filter { it.containsKey(statistic.name) }
                    .map { it[statistic.name] }
                    .map {
                        LOG.debug("Updating metrics reference values for review statistic ${statistic.name}")
                        it?.get(NUMBER_OF_ENDPOINTS)?.set(statistic.numberOfEndpoints)
                        it?.get(MUST_VIOLATIONS)?.set(statistic.mustViolations)
                        it?.get(SHOULD_VIOLATIONS)?.set(statistic.shouldViolations)
                        it?.get(MAY_VIOLATIONS)?.set(statistic.mayViolations)
                        it?.get(HINT_VIOLATIONS)?.set(statistic.hintViolations)
                    }
                    .ifEmpty {
                        LOG.debug("Creating new metrics reference for review statistic ${statistic.name}")
                        val statisticMap = createReferenceMapForStatistic(statistic)
                        statisticsReferences.add(statisticMap)
                        registerGaugeMetric(statisticMap)
                    }
            }
    }

    private fun createReferenceMapForStatistic(statistic: ReviewStatisticsByName): Map<String, Map<String, AtomicLong>> {
        return mapOf(Pair(
            statistic.name,
            mapOf(
                Pair(NUMBER_OF_ENDPOINTS, AtomicLong(statistic.numberOfEndpoints)),
                Pair(MUST_VIOLATIONS, AtomicLong(statistic.mustViolations)),
                Pair(SHOULD_VIOLATIONS, AtomicLong(statistic.shouldViolations)),
                Pair(MAY_VIOLATIONS, AtomicLong(statistic.mayViolations)),
                Pair(HINT_VIOLATIONS, AtomicLong(statistic.hintViolations))
            )
        ))
    }

    private fun registerGaugeMetric(statisticReference: Map<String, Map<String, AtomicLong>>) {
        statisticReference.entries.forEach { entry ->
            entry.value.forEach { metric ->
                val snakeCasedApiName = entry.key.replace(Regex("\\p{Zs}+"), "_").toLowerCase()
                val metricName = "${metricsNamePrefix}${metric.key}"
                Gauge
                    .builder(metricName, metric.value, { v -> v.toDouble() })
                    .tag("api_name", snakeCasedApiName)
                    .register(meterRegistry)
                LOG.debug("Registered micrometer gauge $metricName for api $snakeCasedApiName")
            }
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewMetrics::class.java)
        const val NUMBER_OF_ENDPOINTS = "number_of_endpoints_total"
        const val MUST_VIOLATIONS = "must_violations_total"
        const val SHOULD_VIOLATIONS = "should_violations_total"
        const val MAY_VIOLATIONS = "may_violations_total"
        const val HINT_VIOLATIONS = "hint_violations_total"
    }
}
