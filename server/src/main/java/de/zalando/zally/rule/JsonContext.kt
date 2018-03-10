package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode

/**
 * Context for invoking checks against JsonNode model.
 */
class JsonContext(root: JsonNode, policy: RulesPolicy, details: CheckDetails) :
        Context<JsonNode>(root, policy, details) {

    override fun accepts(): Boolean = accepts(root)

    /**
     * Confirms whether the current rule should be applied to the specified
     * model part, given the current rules policy and any x-zally-ignores
     * entries.
     * @param node the model part to check
     * @return true iff the rule should be applied.
     */
    private fun accepts(node: JsonNode): Boolean {
        val moreIgnores = root.path(zallyIgnoreExtension)
        val localPolicy = if (moreIgnores.isArray) {
            policy.withMoreIgnores(moreIgnores.map(JsonNode::asText))
        } else {
            policy
        }
        return localPolicy.accepts(details.rule)
    }
}