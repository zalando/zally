package de.zalando.zally.dto

import com.fasterxml.jackson.databind.JsonNode

interface LocationResolver {

    fun getLineNumber(path: String): Int?

    companion object {
        val Empty = object : LocationResolver {
            override fun getLineNumber(path: String): Int? = null
        }
    }

}

class MapLocationResolver(val rootNode: JsonNode, val locationMap: Map<JsonNode, Int>) : LocationResolver {

    override fun getLineNumber(path: String): Int? {
        val node = rootNode.at(path)
        return if (!node.isMissingNode) locationMap[node] else null
    }
}