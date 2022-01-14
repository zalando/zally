package org.zalando.zally.rule.api

import org.zalando.zally.rule.api.Severity.HINT
import org.zalando.zally.rule.api.Severity.MUST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SeverityTest {

    private val sorted = Severity.values().toSortedSet()

    @Test
    fun mostSevere() {
        assertEquals(MUST, sorted.first())
    }

    @Test
    fun leastSevere() {
        assertEquals(HINT, sorted.last())
    }
}
