package org.zalando.zally.configuration

import tools.jackson.core.type.TypeReference
import tools.jackson.databind.ObjectMapper
import tools.jackson.databind.SerializationFeature
import tools.jackson.databind.json.JsonMapper
import tools.jackson.dataformat.yaml.YAMLMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Serves the API definition of this server and the Twintip schema discovery document.
 *
 * Replaces the unmaintained `twintip-spring-web` library, with the same endpoints and CORS headers.
 */
@RestController
class SchemaController(
    @Value("\${twintip.yaml}") schema: Resource,
    @Value("\${twintip.mapping:/api}") private val mapping: String,
) {
    private val json =
        JsonMapper
            .builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build()
    private val yaml = YAMLMapper()
    private val node: Map<String, Any> =
        schema.inputStream.use {
            yaml.readValue(it, object : TypeReference<Map<String, Any>>() {})
        }

    @GetMapping("/.well-known/schema-discovery", produces = ["application/json"])
    fun discover() = SchemaDiscovery(schemaUrl = mapping, schemaType = "swagger-2.0")

    @GetMapping("\${twintip.mapping:/api}", produces = ["application/json", "*/*"])
    fun jsonSchema(): ResponseEntity<String> = response(json.writeValueAsString(node))

    @GetMapping("\${twintip.mapping:/api}", produces = ["application/yaml", "application/x-yaml", "text/yaml"])
    fun yamlSchema(): ResponseEntity<String> = response(yaml.writeValueAsString(node))

    private fun response(body: String): ResponseEntity<String> =
        ResponseEntity
            .ok()
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET")
            .header("Access-Control-Max-Age", "3600")
            .header("Access-Control-Allow-Headers", "")
            .body(body)
}
