package de.zalando.zally.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointerExtensionsTest {

    @Test
    fun `toJsonPointer with valid string returns JsonPointer`() {

        val pointer = "/info/version".toJsonPointer()

        assertThat(pointer).hasToString("/info/version")
    }

    @Test
    fun `plus with JsonPointer returns concatenated JsonPointer`() {

        val pointer = "/info".toJsonPointer() + "/version".toJsonPointer()

        assertThat(pointer).hasToString("/info/version")
    }

    @Test
    fun `plus with plain String returns escaped JsonPointer`() {

        val pointer = "/info".toJsonPointer() + "version"

        assertThat(pointer).hasToString("/info/version")
    }

    @Test
    fun `plus with String with slash returns escaped JsonPointer`() {

        val pointer = "/info".toJsonPointer() + "/version"

        assertThat(pointer).hasToString("/info/~1version")
    }

    @Test
    fun `plus with String with tilde returns escaped JsonPointer`() {

        val pointer = "/info".toJsonPointer() + "~version"

        assertThat(pointer).hasToString("/info/~0version")
    }

    @Test
    fun `plus with String with slash and tilde returns escaped JsonPointer`() {

        val pointer = "/info".toJsonPointer() + "~/version"

        assertThat(pointer).hasToString("/info/~0~1version")
    }
}
