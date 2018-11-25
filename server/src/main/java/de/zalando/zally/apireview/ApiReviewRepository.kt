package de.zalando.zally.apireview

import de.zalando.zally.statistic.ReviewStatistics
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import java.time.LocalDate
import java.util.UUID

interface ApiReviewRepository : CrudRepository<ApiReview, Long> {

    @Query(
        """
        SELECT new de.zalando.zally.statistic.ReviewStatistics(
	        COUNT(r) AS totalReviews,
	        COUNT(DISTINCT r.name) AS totalReviewDeduplicated,
	        COALESCE(SUM(CASE WHEN r.isSuccessfulProcessed = 'True' THEN 1 ELSE 0 END),0) AS successfulReviews,
	        COALESCE(SUM(r.numberOfEndpoints),0) AS numberOfEndpoints,
	        COALESCE(SUM(r.mustViolations),0) AS mustViolations,
	        COALESCE(SUM(r.shouldViolations),0) AS shouldViolations,
	        COALESCE(SUM(r.mayViolations),0) AS mayViolations,
	        COALESCE(SUM(r.hintViolations),0) AS hintViolations)
        FROM de.zalando.zally.apireview.ApiReview r
        WHERE day >= :from AND day <= :to AND user_agent LIKE :userAgent
    """
    )
    fun getReviewStatistics(
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
        @Param("userAgent") userAgent: String = "%"
    ): ReviewStatistics

    fun findByExternalId(externalId: UUID): ApiReview?
}
