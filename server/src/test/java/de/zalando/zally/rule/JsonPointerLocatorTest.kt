package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
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

        assertEquals(null, locator.locate(JsonPointer.compile("/a")))
    }

    @Test
    fun `locate with yaml and path to key value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            a: value
            """.trimIndent()
        )

        assertEquals(1..1, locator.locate(JsonPointer.compile("/a")))
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

        assertEquals(1..3, locator.locate(JsonPointer.compile("/a")))
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

        assertEquals(4..5, locator.locate(JsonPointer.compile("/a/b/c/d")))
    }

    @Test
    fun `locate with yaml and path to indexed value returns lines`() {
        @Language("YAML")
        val locator = JsonPointerLocator(
            """
            - value
            """.trimIndent()
        )

        assertEquals(1..1, locator.locate(JsonPointer.compile("/0")))
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

        assertEquals(2..3, locator.locate(JsonPointer.compile("/c/b")))
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

        assertEquals(5..6, locator.locate(JsonPointer.compile("/c/b")))
    }
}
