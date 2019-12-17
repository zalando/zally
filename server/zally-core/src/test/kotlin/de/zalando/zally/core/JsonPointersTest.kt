package de.zalando.zally.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class JsonPointersTest {

    @Test
    fun `converts response--x--content--media-type--schema to schema`() {
        val pointer = "/paths/~1items/get/responses/default/content/application~1json/schema".toJsonPointer()
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/responses/default/schema")
    }

    @Test
    fun `converts response--x--content--media-type to response--x`() {
        val pointer = "/paths/~1items/get/responses/200/content/application~1json".toJsonPointer()
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/responses/200")
    }

    // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
    @Test
    fun `converts requestBody--content--media-type to consumes`() {
        val pointer = "/paths/~1items/get/requestBody/content/application~1json".toJsonPointer()
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/paths/~1items/get/consumes")
    }

    @Test
    fun `converts components--securitySchemes--X--flows--X--scopes to securityDefinitions--implicit-oauth2--scopes`() {
        val pointer = "/components/securitySchemes/implicit-oauth2/flows/implicit/scopes".toJsonPointer()
        val converted = JsonPointers.convertPointer(pointer)
        assertThat(converted).hasToString("/securityDefinitions/implicit-oauth2/scopes")
    }
}
