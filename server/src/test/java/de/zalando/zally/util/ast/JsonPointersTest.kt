package de.zalando.zally.util.ast

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointersTest {
    @Test
    fun `convert OpenAPI 3 paths pointer`() {
        val pointer = "#/paths/~1items/get/responses/default/content/application~1json/schema"
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).isEqualTo("#/paths/~1items/get/responses/default/schema")
    }
}
