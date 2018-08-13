package de.zalando.zally.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

object TestDateUtil {

    fun now(): OffsetDateTime {
        return Instant.now().atOffset(ZoneOffset.UTC)
    }

    fun yesterday(): OffsetDateTime {
        return now().minusDays(1L)
    }

    fun tomorrow(): OffsetDateTime {
        return now().plusDays(1L)
    }
}
