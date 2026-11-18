package org.zalando.zally.util

import tools.jackson.core.JsonParser
import tools.jackson.core.TreeNode
import tools.jackson.databind.DeserializationContext
import tools.jackson.databind.ValueDeserializer

class JsonRawValueDeserializer : ValueDeserializer<String>() {
    override fun deserialize(
        jp: JsonParser,
        context: DeserializationContext,
    ): String = jp.readValueAsTree<TreeNode>().toString()
}
