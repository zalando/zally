package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonRulesValidator(@Autowired rules: RulesManager) : RulesValidator<JsonNode>(rules) {

    private val reader = ObjectTreeReader()

    override fun parse(content: String): JsonNode? {
        return try {
            reader.read(content)
        } catch (e: Exception) {
            null
        }
    }

    override fun ignores(root: JsonNode): List<String> {
        val ignores = root.path(zallyIgnoreExtension)
        return if (ignores.isArray) {
            ignores.map(JsonNode::asText)
        } else {
            emptyList()
        }
    }
}
