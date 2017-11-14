package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonRulesValidator(@Autowired rules: List<JsonRule>,
                         @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<JsonRule, JsonNode>(rules, invalidApiRule) {

    private val reader = ObjectTreeReader()

    override fun parse(content: String): JsonNode = reader.read(content)

    override fun ignores(root: JsonNode): List<String> {
        val ignores = root.path(zallyIgnoreExtension)
        return if (ignores.isArray) {
            ignores.map(JsonNode::asText)
        } else {
            emptyList()
        }
    }

    override fun validator(root: JsonNode): (JsonRule) -> Iterable<Violation> {
        return {
            it.validate(root)
        }
    }
}
