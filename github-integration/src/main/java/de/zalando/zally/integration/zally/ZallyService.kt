package de.zalando.zally.integration.zally

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import de.zalando.zally.integration.config.logger
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class ZallyService(@Qualifier("yamlObjectMapper") private val yamlObjectMapper: ObjectMapper,
                   private val jsonObjectMapper: ObjectMapper,
                   private val zallyClient: ZallyClient) {

    private val log by logger()
    private val jsonNodeFactory = JsonNodeFactory.instance

    fun validate(swaggerFile: String, ignoredRules: List<String>): ApiDefinitionResponse {
        val apiDefinitionsTree = yamlObjectMapper.readTree(swaggerFile)

        val wrapper = jsonNodeFactory.objectNode()
        wrapper.set("api_definition", apiDefinitionsTree)

        if (!ignoredRules.isEmpty()) {
            wrapper.putArray("ignoreRules").addAll(ignoredRules.map {
                jsonNodeFactory.textNode(it)
            })
        }

        val request = jsonObjectMapper.writeValueAsString(wrapper)

        val validationResult = zallyClient.validate(request)

        log.info("Zally validation response {}", validationResult)
        return validationResult
    }

}