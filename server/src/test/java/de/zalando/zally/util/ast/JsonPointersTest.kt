package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointersTest {

    @Test
    fun `converts response--x--content--media-type--schema to schema`() {
        val pointer = JsonPointer.compile("/paths/~1items/get/responses/default/content/application~1json/schema")
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/responses/default/schema")
    }

    @Test
    fun `converts response--x--content--media-type to response--x`() {
        val pointer = JsonPointer.compile("/paths/~1items/get/responses/200/content/application~1json")
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/responses/200")
    }

    // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
    @Test
    fun `converts requestBody--content--media-type to consumes`() {
        val pointer = JsonPointer.compile("/paths/~1items/get/requestBody/content/application~1json")
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/consumes")
    }
}
