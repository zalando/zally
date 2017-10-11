package de.zalando.zally.dto

import com.fasterxml.jackson.databind.JsonNode

interface LocationResolver {

    fun getLineNumber(pointer: String): Int?

    companion object {
        val Empty = object : LocationResolver {
            override fun getLineNumber(pointer: String): Int? = null
        }
    }

}

class MapLocationResolver(val rootNode: JsonNode, val locationMap: Map<JsonNode, Int>) : LocationResolver {

    override fun getLineNumber(pointer: String): Int? {
        val node = rootNode.at(pointer)
        return if (!node.isMissingNode) locationMap[node] else null
    }
}