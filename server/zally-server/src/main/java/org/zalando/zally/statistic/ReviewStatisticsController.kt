package org.zalando.zally.statistic

import org.zalando.zally.apireview.ApiReviewRepository
import org.zalando.zally.exception.InsufficientTimeIntervalParameterException
import org.zalando.zally.exception.TimeParameterIsInTheFutureException
import org.slf4j.LoggerFactory
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@CrossOrigin
@RestController
class ReviewStatisticsController(private val apiReviewRepository: ApiReviewRepository) {

    @ResponseBody
    @GetMapping("/review-statistics")
    fun retrieveReviewStatistics(
        @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?,
        @RequestParam(value = "user_agent", required = false) userAgent: String?
    ): ReviewStatistics {

        if (from != null && from.isAfter(today())) {
            throw TimeParameterIsInTheFutureException()
        }

        if (to != null && from == null) {
            throw InsufficientTimeIntervalParameterException()
        }

        val apiReviewStatistics = if (from != null) {
            apiReviewRepository.findByDayBetween(userAgent, from, to ?: today())
        } else {
            apiReviewRepository.findFromLastWeek(userAgent)
        }

        LOG.info(
            "Found {} api reviews from {} to {} user_agent {}",
            apiReviewStatistics.totalReviews,
            from,
            to,
            userAgent
        )
        return apiReviewStatistics
    }

    private fun ApiReviewRepository.findFromLastWeek(userAgent: String?): ReviewStatistics {
        val today = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()

        return when {
            userAgent == null || userAgent.isEmpty() -> getReviewStatistics(today.minusDays(7L), today)
            else -> getReviewStatistics(today.minusDays(7L), today, userAgent)
        }
    }

    private fun ApiReviewRepository.findByDayBetween(
        userAgent: String?,
        from: LocalDate,
        to: LocalDate
    ): ReviewStatistics {
        return when {
            userAgent == null || userAgent.isEmpty() -> getReviewStatistics(from, to)
            else -> getReviewStatistics(from, to, userAgent)
        }
    }

    private fun today(): LocalDate {
        return Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewStatisticsController::class.java)
    }
}
