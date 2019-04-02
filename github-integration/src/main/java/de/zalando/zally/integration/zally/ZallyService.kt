package de.zalando.zally.integration.zally

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ZallyService(
    @Qualifier("yamlObjectMapper") private val yamlObjectMapper: ObjectMapper,
    private val jsonObjectMapper: ObjectMapper,
    private val zallyClient: ZallyClient
) {

    private val log = KotlinLogging.logger {}
    private val jsonNodeFactory = JsonNodeFactory.instance

    fun validate(swaggerFile: String): ApiDefinitionResponse {
        val apiDefinitionsTree = yamlObjectMapper.readTree(swaggerFile)

        val wrapper = jsonNodeFactory.objectNode()
        wrapper.set("api_definition", apiDefinitionsTree)

        val request = jsonObjectMapper.writeValueAsString(wrapper)

        val validationResult = zallyClient.validate(request)

        log.info("Zally validation response {}", validationResult)
        return validationResult
    }
}