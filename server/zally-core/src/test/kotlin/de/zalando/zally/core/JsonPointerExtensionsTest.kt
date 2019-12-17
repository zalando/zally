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

    @Test
    fun `toSwaggerJsonPointer with unsupported pointer returns null`() {
        val pointer = "/info".toJsonPointer()
        val converted = pointer.toSwaggerJsonPointer()
        assertThat(converted).isNull()
    }

    @Test
    fun `toSwaggerJsonPointer response--x--content--media-type--schema to schema`() {
        val pointer = "/paths/~1items/get/responses/default/content/application~1json/schema".toJsonPointer()
        val converted = pointer.toSwaggerJsonPointer()
        assertThat(converted).hasToString("/paths/~1items/get/responses/default/schema")
    }

    @Test
    fun `toSwaggerJsonPointer response--x--content--media-type to response--x`() {
        val pointer = "/paths/~1items/get/responses/200/content/application~1json".toJsonPointer()
        val converted = pointer.toSwaggerJsonPointer()
        assertThat(converted).hasToString("/paths/~1items/get/responses/200")
    }

    // VERB/requestBody/content/MEDIA_TYPE --> VERB/consumes
    @Test
    fun `toSwaggerJsonPointer requestBody--content--media-type to consumes`() {
        val pointer = "/paths/~1items/get/requestBody/content/application~1json".toJsonPointer()
        val converted = pointer.toSwaggerJsonPointer()
        assertThat(converted).hasToString("/paths/~1items/get/consumes")
    }

    @Test
    fun `toSwaggerJsonPointer components--securitySchemes--X--flows--X--scopes to securityDefinitions--implicit-oauth2--scopes`() {
        val pointer = "/components/securitySchemes/implicit-oauth2/flows/implicit/scopes".toJsonPointer()
        val converted = pointer.toSwaggerJsonPointer()
        assertThat(converted).hasToString("/securityDefinitions/implicit-oauth2/scopes")
    }
}
