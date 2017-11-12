package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode

abstract class RulesValidator<RuleT>(val rules: List<RuleT>, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    private val zallyIgnoreExtension = "x-zally-ignore"

    private val reader = ObjectTreeReader()

    final override fun validate(swaggerContent: String, requestPolicy: RulesPolicy): List<Violation> {
        return try {
            val json = reader.read(swaggerContent)
            val documentPolicy = documentPolicy(json, requestPolicy)

            rules
                    .filter { documentPolicy.accepts(it) }
                    .flatMap(createRuleChecker(json))
                    .sortedBy(Violation::violationType)
        } catch (e: Exception) {
            listOf(invalidApiRule.getGeneralViolation())
        }
    }

    private fun documentPolicy(json: JsonNode, requestPolicy: RulesPolicy): RulesPolicy {
        val ignoredCodes = json.path(zallyIgnoreExtension)
        return if (ignoredCodes.isArray) {
            val moreIgnores = ignoredCodes.map { it.asText() }
            requestPolicy.withMoreIgnores(moreIgnores)
        } else {
            requestPolicy
        }
    }

    @Throws(java.lang.Exception::class)
    abstract fun createRuleChecker(json: JsonNode): (RuleT) -> Iterable<Violation>
}
