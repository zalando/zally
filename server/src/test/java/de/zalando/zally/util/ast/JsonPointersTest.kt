package de.zalando.zally.util.ast

import com.fasterxml.jackson.core.JsonPointer
import io.swagger.models.Swagger
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointersTest {
    @Test
    fun `convert OpenAPI 3 paths pointer`() {
        val pointer = JsonPointer.compile("/paths/~1items/get/responses/default/content/application~1json/schema")
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/responses/default/schema")
    }

    @Test
    fun `escape() turns getInfo to info`() {
        val method = Swagger::class.java.methods.first { it.name == "getInfo" }

        val pointer = JsonPointers.escape(method)

        assertThat(pointer).hasToString("/info")
    }

    @Test
    fun `escape() turns get(KEY) to KEY`() {
        val method = Map::class.java.methods.first { it.name == "get" }

        val pointer = JsonPointers.escape(method, "KEY")

        assertThat(pointer).hasToString("/KEY")
    }
}
