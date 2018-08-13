package de.zalando.zally.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

import java.io.IOException

class JsonRawValueDeserializer : JsonDeserializer<String>() {

    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, context: DeserializationContext): String {
        return jp.readValueAsTree<TreeNode>().toString()
    }
}
