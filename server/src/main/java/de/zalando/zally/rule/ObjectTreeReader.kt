package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.parser.util.DeserializationUtils
import java.net.URL

class ObjectTreeReader {

    fun read(location: URL): JsonNode =
            read(location.readText(), location.toString())

    fun read(content: String, location: String = "memory"): JsonNode =
            DeserializationUtils.deserializeIntoTree(content, location)
}
