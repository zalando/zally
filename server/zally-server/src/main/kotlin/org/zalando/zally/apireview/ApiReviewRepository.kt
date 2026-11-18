package org.zalando.zally.apireview

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.zalando.zally.statistic.ReviewStatistics
import java.time.LocalDate
import java.util.UUID

interface ApiReviewRepository : CrudRepository<ApiReview, Long> {
    @Query(
        """
        SELECT new org.zalando.zally.statistic.ReviewStatistics(
	        COUNT(r),
	        COUNT(DISTINCT r.name),
	        COALESCE(SUM(CASE WHEN r.isSuccessfulProcessed = true THEN 1 ELSE 0 END),0),
	        COALESCE(SUM(r.numberOfEndpoints),0),
	        COALESCE(SUM(r.mustViolations),0),
	        COALESCE(SUM(r.shouldViolations),0),
	        COALESCE(SUM(r.mayViolations),0),
	        COALESCE(SUM(r.hintViolations),0))
        FROM org.zalando.zally.apireview.ApiReview r
        WHERE r.day >= :from AND r.day <= :to AND r.userAgent LIKE :userAgent
    """,
    )
    fun getReviewStatistics(
        @Param("from") from: LocalDate,
        @Param("to") to: LocalDate,
        @Param("userAgent") userAgent: String = "%",
    ): ReviewStatistics

    /**
     * Find ApiReview instance by it's externalId UUID.
     *
     * @return the found ApiReview or null.
     */
    fun findByExternalId(externalId: UUID): ApiReview?
}
