package org.zalando.zally.core.util

import org.zalando.zally.core.util.PatternUtil.isPathVariable
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Unit tests for patterns utility
 */
class PatternUtilTest {

    @Test
    fun checkIsPathVariable() {
        assertTrue(isPathVariable("{test}"))
        assertFalse(isPathVariable("{}"))
        assertFalse(isPathVariable(" { } "))
        assertFalse(isPathVariable("abc"))
        assertFalse(isPathVariable("{test"))
        assertFalse(isPathVariable("test}"))
    }
}
