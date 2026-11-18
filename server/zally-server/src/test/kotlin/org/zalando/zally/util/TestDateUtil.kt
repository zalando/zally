package org.zalando.zally.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class TestDateUtil {
    companion object {
        fun now(): OffsetDateTime = Instant.now().atOffset(ZoneOffset.UTC)

        fun yesterday(): OffsetDateTime = now().minusDays(1L)

        fun tomorrow(): OffsetDateTime = now().plusDays(1L)
    }
}
