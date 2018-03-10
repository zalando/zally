package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given JsonNode definition based
 * on set of rules.
 */
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

    override fun context(root: JsonNode, policy: RulesPolicy, details: CheckDetails): JsonContext =
            JsonContext(root, policy, details)
}
