package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonRulesValidator(@Autowired rules: List<Rule>,
                         @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<Rule, JsonNode>(rules, invalidApiRule) {

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
