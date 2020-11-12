package org.zalando.zally.apireview

import org.zalando.zally.rule.api.Severity
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.EnumMap
import kotlin.math.min

class Score {
    companion object {
        /**
         * A highly sophisticated ;) calculation of a float value between 0 and 1 that represents a percentage which describes how good the result of the API Linter is.
         */
        fun forLinterResult(apiReview: ApiReview): Float {
            val violationsPerSeverity = apiReview.ruleViolations.groupBy { it.type }
            return calculateScore(violationsPerSeverity)
        }

        private fun calculateScore(violationsPerSeverity: Map<Severity, List<RuleViolation>>): Float {
            val distinctViolationsPerSeverity = calculateDistinctViolationsPerSeverity(violationsPerSeverity)

            var score = 1.0f
            score -= calculateHandicap(distinctViolationsPerSeverity[Severity.MUST], 0.2f, 0.8f)
            score -= calculateHandicap(distinctViolationsPerSeverity[Severity.SHOULD], 0.05f, 0.15f)
            score -= calculateHandicap(distinctViolationsPerSeverity[Severity.MAY], 0.01f, 0.05f)
            return round(score)
        }

        private fun calculateDistinctViolationsPerSeverity(violationsPerSeverity: Map<Severity, List<RuleViolation>>): Map<Severity, Long> {
            val distinctViolationsPerSeverity = EnumMap<Severity, Long>(Severity::class.java)
            violationsPerSeverity.keys.forEach {
                val distinctViolationsCount = violationsPerSeverity[it]?.stream()
                    ?.map(RuleViolation::ruleTitle)
                    ?.distinct()
                    ?.count()
                distinctViolationsPerSeverity[it] = distinctViolationsCount
            }
            return distinctViolationsPerSeverity
        }

        private fun calculateHandicap(distinctViolations: Long?, weight: Float, maxHandicap: Float): Float {
            var handicap = 0.0f
            if (distinctViolations != null && distinctViolations > 0) {
                handicap += weight * distinctViolations
            }
            return min(maxHandicap, handicap)
        }

        private fun round(value: Float): Float {
            return BigDecimal(value.toString())
                .setScale(2, RoundingMode.HALF_UP)
                .toFloat()
        }
    }
}
