package org.zalando.zally.core

import org.intellij.lang.annotations.Language
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("UndocumentedPublicClass", "UndocumentedPublicFunction")
class JsonPointerLocatorTest {

    @Test
    fun `locate with json and path returns null`() {
        @Language("JSON")
        val locator = JsonPointerLocator(
            """
            {
              "a": "value"
            }
            """.trimIndent()
        )

        assertEquals(null, locator.locate("/a".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to key value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a: value
            """.trimIndent()
        )

        assertEquals(1..1, locator.locate("/a".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to multiline key value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a:

              value
            """.trimIndent()
        )

        assertEquals(1..3, locator.locate("/a".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to deep key value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a:
              b:
                c:
                  d:
                    value
            """.trimIndent()
        )

        assertEquals(4..5, locator.locate("/a/b/c/d".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to indexed value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            - value
            """.trimIndent()
        )

        assertEquals(1..1, locator.locate("/0".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to anchor and reference returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a: &address
              b:
                value

            c:
              *address
            """.trimIndent()
        )

        assertEquals(2..3, locator.locate("/c/b".toJsonPointer()))
    }

    @Test
    fun `locate with yaml and path to anchor and merge returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a: &address
              b:
                value

            c:
              <<: *address
            """.trimIndent()
        )

        assertEquals(5..6, locator.locate("/c/b".toJsonPointer()))
    }
}
