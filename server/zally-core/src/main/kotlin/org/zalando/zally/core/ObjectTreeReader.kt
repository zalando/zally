package org.zalando.zally.core

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.parser.util.DeserializationUtils
import java.net.URL

/** Utility for reading JSON and YAML into JsonNode trees */
class ObjectTreeReader {

    /**
     * Reads the JSON or YAML content of a URL into a JsonNode tree.
     * @param location the URL to read from
     * @return a tree of JsonNodes
     */
    fun read(location: URL): JsonNode =
        read(location.readText(), location.toString())

    /**
     * Reads JSON or YAML content into a JsonNode tree.
     * @param content JSON or YAML content
     * @param location where errors should say the location was, defaults to "memory"
     * @return a tree of JsonNodes
     */
    fun read(content: String, location: String = "memory"): JsonNode =
        DeserializationUtils.deserializeIntoTree(content, location)
}
