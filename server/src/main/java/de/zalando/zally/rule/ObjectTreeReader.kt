package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import java.net.URL

class ObjectTreeReader {

    private val jsonMapper = ObjectMapper()
    private val yamlMapper = ObjectMapper(YAMLFactory())

    fun read(content: String): JsonNode =
            if (isJson(content))
                readJson(content)
            else
                readYaml(content)

    fun readJson(content: String): JsonNode =
            jsonMapper.readTree(content)

    fun readYaml(content: String): JsonNode =
            yamlMapper.readTree(content)

    fun readYaml(yamlUrl: URL): JsonNode =
            yamlMapper.readTree(yamlUrl)

    fun readJson(jsonUrl: URL): JsonNode =
            jsonMapper.readTree(jsonUrl)

    fun read(parser: JsonParser): JsonNode =
            jsonMapper.readTree(parser)

    fun isJson(specContent: String): Boolean =
            specContent.firstOrNull { !it.isWhitespace() } == '{'

    fun getParser(specContent: String): JsonParser =
            if (isJson(specContent))
                jsonMapper.factory.createParser(specContent)
            else
                yamlMapper.factory.createParser(specContent)
}
