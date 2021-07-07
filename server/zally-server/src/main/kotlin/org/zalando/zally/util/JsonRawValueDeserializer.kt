package org.zalando.zally.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

class JsonRawValueDeserializer : JsonDeserializer<String>() {

    override fun deserialize(jp: JsonParser, context: DeserializationContext): String =
        jp.readValueAsTree<TreeNode>().toString()
}
