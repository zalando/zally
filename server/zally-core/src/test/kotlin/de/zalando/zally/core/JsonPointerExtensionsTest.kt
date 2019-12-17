package de.zalando.zally.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointerExtensionsTest {

    @Test
    fun `toJsonPointer with valid string returns JsonPointer`() {

        val pointer = "/info/version".toJsonPointer()

        assertThat(pointer).hasToString("/info/version")
    }
}
