package de.zalando.zally.apireview;

import org.springframework.data.repository.CrudRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Collection;

public interface ApiReviewRepository extends CrudRepository<ApiReview, Long> {

    Collection<ApiReview> findByDayBetween(LocalDate from, LocalDate to);

    Collection<ApiReview> findByUserAgentAndDayBetween(String userAgent, LocalDate from, LocalDate to);

    default Collection<ApiReview> findFromLastWeek(String userAgent) {
        final LocalDate today = Instant.now().atOffset(ZoneOffset.UTC).toLocalDate();

        return userAgent == null || userAgent.isEmpty()
            ? findByDayBetween(today.minusDays(7L), today)
            : findByUserAgentAndDayBetween(userAgent, today.minusDays(7L), today);
    }

    default Collection<ApiReview> findByDayBetween(String userAgent, LocalDate from, LocalDate to) {
        return userAgent == null || userAgent.isEmpty()
            ? findByDayBetween(from, to)
            : findByUserAgentAndDayBetween(userAgent, from, to);
    }
}
