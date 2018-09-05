package de.zalando.zally.apireview

import org.springframework.data.repository.CrudRepository
import java.time.LocalDate

interface ApiReviewRepository : CrudRepository<ApiReview, Long> {

    fun findByDayBetween(from: LocalDate, to: LocalDate): Collection<ApiReview>

    fun findByUserAgentAndDayBetween(userAgent: String, from: LocalDate, to: LocalDate): Collection<ApiReview>
}
