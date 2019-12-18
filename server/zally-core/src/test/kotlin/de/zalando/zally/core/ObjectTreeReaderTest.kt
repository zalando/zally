package de.zalando.zally.core

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/** Tests for ObjectTreeReader utility */
class ObjectTreeReaderTest {

    private val cut = ObjectTreeReader()

    /** Tests that basic JSON is supported */
    @Test
    fun readSupportsJson() {
        val contents = """
            {
              "swagger": "2.0",
              "info": {
                "title": "Things API",
                "description": "Description of things",
                "version": "1.0.0"
              }
            }
            """.trimIndent()

        val node = cut.read(contents)

        assertThat(
            node
                .path("swagger")
                .textValue()
        )
            .hasToString("2.0")
        assertThat(
            node
                .path("info")
                .path("title")
                .textValue()
        )
            .hasToString("Things API")
        assertThat(
            node
                .path("info")
                .path("version")
                .textValue()
        )
            .hasToString("1.0.0")
    }

    /** Tests that basic YAML is supported */
    @Test
    fun readSupportsYaml() {
        val contents = """
            swagger: '2.0'
            info:
              title: Things API
              description: Description of things
              version: 1.0.0
            """.trimIndent()

        val node = cut.read(contents)

        assertThat(
            node
                .path("swagger")
                .textValue()
        )
            .hasToString("2.0")
        assertThat(
            node
                .path("info")
                .path("title")
                .textValue()
        )
            .hasToString("Things API")
        assertThat(
            node
                .path("info")
                .path("version")
                .textValue()
        )
            .hasToString("1.0.0")
    }

    /** Tests that advanced YAML is supported */
    @Test
    fun readSupportsYamlAnchorsAndReferences() {

        val contents = """
            Idable:
              type: object
              properties:
                id: &standard-id-property
                  type: string
                  format: uuid
            WriteThing:
              type: object
              properties: &thing-editable-properties
                name:
                  type: string
                description:
                  type: string
            ReadThing:
              type: object
              properties:
                id: *standard-id-property
                <<: *thing-editable-properties
            """.trimIndent()

        val node = cut.read(contents)

        assertThat(
            node
                .path("ReadThing")
                .path("properties")
                .path("id")
                .path("format")
                .textValue()
        )
            .hasToString("uuid")

        assertThat(
            node
                .path("ReadThing")
                .path("properties")
                .path("name")
                .path("type")
                .textValue()
        )
            .hasToString("string")
    }
}
