package de.zalando.zally.rule.api

import de.zalando.zally.rule.api.Severity.HINT
import de.zalando.zally.rule.api.Severity.MUST
import de.zalando.zally.rule.api.Severity.values
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Arrays.asList
import java.util.TreeSet

class SeverityTest {

    private val sorted = TreeSet(asList(*values()))

    @Test
    fun mostSevere() {
        assertEquals(MUST, sorted.first())
    }

    @Test
    fun leastSevere() {
        assertEquals(HINT, sorted.last())
    }
}
