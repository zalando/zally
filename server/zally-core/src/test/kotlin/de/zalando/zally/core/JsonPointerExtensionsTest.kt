package de.zalando.zally.core

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class JsonPointerExtensionsTest {

    @Test
    fun `toJsonPointer with valid string returns JsonPointer`() {
        assertThat("/info/version".toJsonPointer())
            .hasToString("/info/version")
    }

    @Test
    fun `toJsonPointer with invalid string throws exception`() {
        assertThatThrownBy {
            "info version".toJsonPointer()
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("expression must start with '/'")
    }

    @Test
    fun `toEscapedJsonPointer with plain String returns escaped JsonPointer`() {
        assertThat("version".toEscapedJsonPointer())
            .hasToString("/version")
    }

    @Test
    fun `toEscapedJsonPointer with String with slash returns escaped JsonPointer`() {
        assertThat("/version".toEscapedJsonPointer())
            .hasToString("/~1version")
    }

    @Test
    fun `toEscapedJsonPointer with String with tilde returns escaped JsonPointer`() {
        assertThat("~version".toEscapedJsonPointer())
            .hasToString("/~0version")
    }

    @Test
    fun `toEscapedJsonPointer with String with slash and tilde returns escaped JsonPointer`() {
        assertThat("~/version".toEscapedJsonPointer())
            .hasToString("/~0~1version")
    }

    @Test
    fun `plus with JsonPointer returns concatenated JsonPointer`() {
        assertThat("/info".toJsonPointer() + "/version".toJsonPointer())
            .hasToString("/info/version")
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
