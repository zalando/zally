package org.zalando.zally.rule.api

import org.zalando.zally.rule.api.Severity.HINT
import org.zalando.zally.rule.api.Severity.MUST
import org.junit.Assert.assertEquals
import org.junit.Test

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
