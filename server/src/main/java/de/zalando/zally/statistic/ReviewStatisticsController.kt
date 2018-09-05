package de.zalando.zally.statistic

import de.zalando.zally.apireview.ApiReview
import de.zalando.zally.apireview.ApiReviewRepository
import de.zalando.zally.exception.InsufficientTimeIntervalParameterException
import de.zalando.zally.exception.TimeParameterIsInTheFutureException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
class ReviewStatisticsController @Autowired
constructor(private val apiReviewRepository: ApiReviewRepository) {

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

        val apiReviews = if (from != null) {
            apiReviewRepository.findByDayBetween(userAgent, from, to ?: today())
        } else {
            apiReviewRepository.findFromLastWeek(userAgent)
        }

        LOG.info("Found {} api reviews from {} to {} user_agent {}", apiReviews.size, from, to, userAgent)
        return ReviewStatistics(apiReviews)
    }

    private fun ApiReviewRepository.findFromLastWeek(userAgent: String?): Collection<ApiReview> {
        val today = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()

        return when {
            userAgent == null || userAgent.isEmpty() -> findByDayBetween(today.minusDays(7L), today)
            else -> findByUserAgentAndDayBetween(userAgent, today.minusDays(7L), today)
        }
    }

    private fun ApiReviewRepository.findByDayBetween(userAgent: String?, from: LocalDate, to: LocalDate): Collection<ApiReview> {
        return when {
            userAgent == null || userAgent.isEmpty() -> findByDayBetween(from, to)
            else -> findByUserAgentAndDayBetween(userAgent, from, to)
        }
    }

    private fun today(): LocalDate {
        return Instant.now().atOffset(ZoneOffset.UTC).toLocalDate()
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(ReviewStatisticsController::class.java)
    }
}
