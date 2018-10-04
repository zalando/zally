package de.zalando.zally.util

import de.zalando.zally.util.PatternUtil.hasTrailingSlash
import de.zalando.zally.util.PatternUtil.isCamelCase
import de.zalando.zally.util.PatternUtil.isHyphenatedCamelCase
import de.zalando.zally.util.PatternUtil.isPascalCase
import de.zalando.zally.util.PatternUtil.isPathVariable
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for patterns utility
 */
class PatternUtilTest {

    @Test
    fun checkHasTrailingSlash() {
        assertTrue(hasTrailingSlash("blah/"))
        assertFalse(hasTrailingSlash("blah"))
    }

    @Test
    fun checkIsPathVariable() {
        assertTrue(isPathVariable("{test}"))
        assertFalse(isPathVariable("{}"))
        assertFalse(isPathVariable(" { } "))
        assertFalse(isPathVariable("abc"))
        assertFalse(isPathVariable("{test"))
        assertFalse(isPathVariable("test}"))
    }

    @Test
    fun checkIsCamelCase() {
        assertTrue(isCamelCase("testCase"))
        assertFalse(isCamelCase("TestCase"))
    }

    @Test
    fun checkIsPascalCase() {
        assertTrue(isPascalCase("TestCase"))
        assertFalse(isPascalCase("testCase"))
    }

    @Test
    fun checkIsHyphenatedCamelCase() {
        assertTrue(isHyphenatedCamelCase("test-Case"))
        assertFalse(isHyphenatedCamelCase("Test-Case"))
        assertFalse(isHyphenatedCamelCase("testCase"))
        assertFalse(isHyphenatedCamelCase("TestCase"))
    }
}
