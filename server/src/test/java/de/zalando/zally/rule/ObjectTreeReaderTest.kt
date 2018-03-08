package de.zalando.zally.rule

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ObjectTreeReaderTest {

    private val cut = ObjectTreeReader()

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

        assertThat(node).hasToString("{\"swagger\":\"2.0\",\"info\":{\"title\":\"Things API\",\"description\":\"Description of things\",\"version\":\"1.0.0\"}}")
    }

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

        assertThat(node).hasToString("{\"swagger\":\"2.0\",\"info\":{\"title\":\"Things API\",\"description\":\"Description of things\",\"version\":\"1.0.0\"}}")
    }

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

        val node = cut.read(contents).path("ReadThing").path("properties")

        assertThat(node).hasToString("{\"description\":{\"type\":\"string\"},\"name\":{\"type\":\"string\"},\"id\":{\"type\":\"string\",\"format\":\"uuid\"}}")
    }
}
