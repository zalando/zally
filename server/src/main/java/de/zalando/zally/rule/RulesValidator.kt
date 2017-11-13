package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode

abstract class RulesValidator<RuleT>(val rules: List<RuleT>, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    private val reader = ObjectTreeReader()

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Violation> {
        val json = reader.read(content)

        val contentPolicy = rulesPolicy(json, requestPolicy)

        return try {
            rules
                    .filter(contentPolicy::accepts)
                    .flatMap(validator(json))
                    .sortedBy(Violation::violationType)
        } catch (e: Exception) {
            listOf(invalidApiRule.getGeneralViolation())
        }
    }

    private fun rulesPolicy(json: JsonNode, requestPolicy: RulesPolicy): RulesPolicy {
        val node = json.path("x-zally-ignore")
        return if (node.isArray) {
            requestPolicy.withMoreIgnores(node.map(JsonNode::asText))
        } else {
            requestPolicy
        }
    }

    @Throws(java.lang.Exception::class)
    abstract fun validator(content: JsonNode): (RuleT) -> Iterable<Violation>
}
