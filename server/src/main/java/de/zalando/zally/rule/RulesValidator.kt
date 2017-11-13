package de.zalando.zally.rule

abstract class RulesValidator<RuleT>(val rules: List<RuleT>, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    final override fun validate(swaggerContent: String, requestPolicy: RulesPolicy): List<Violation> {
        val ruleChecker = try {
            createRuleChecker(swaggerContent)
        } catch (e: Exception) {
            return listOf(invalidApiRule.getGeneralViolation())
        }
        return rules
                .filter { requestPolicy.accepts(it) }
                .flatMap(ruleChecker)
                .sortedBy(Violation::violationType)
    }

    @Throws(java.lang.Exception::class)
    abstract fun createRuleChecker(swaggerContent: String): (RuleT) -> Iterable<Violation>
}
