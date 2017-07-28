package de.zalando.zally.rule

import de.zalando.zally.dto.LocationResolver

abstract class RulesValidator<RuleT>(val rules: List<RuleT>, val rulesPolicy: RulesPolicy, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    final override fun validate(swaggerContent: String, ignoreRules: List<String>, locationResolver: LocationResolver): List<Violation> {
        val ruleChecker = try {
            createRuleChecker(swaggerContent)
        } catch (e: Exception) {
            return listOf(invalidApiRule.getGeneralViolation())
        }
        return rules
                .filter { it.code !in ignoreRules }
                .filter { rulesPolicy.accepts(it) }
                .flatMap(ruleChecker)
                .map { enrichWithLineNumbers(it, locationResolver) }
                .sortedBy(Violation::violationType)
    }

    private fun enrichWithLineNumbers(violation: Violation, locationResolver: LocationResolver): Violation {
        if (violation.specPointers.size > 0 && violation.paths.size == violation.specPointers.size) {
            val enrichedPaths = violation.paths.zip(violation.specPointers).map { (path, pointer) ->
                val lineNumber = locationResolver.getLineNumber(pointer)
                if (lineNumber != null) "$path\t\t[line#: $lineNumber]" else path
            }
            return violation.copy(paths = enrichedPaths)
        } else {
            return violation
        }
    }

    @Throws(java.lang.Exception::class)
    abstract fun createRuleChecker(swaggerContent: String): (RuleT) -> Iterable<Violation>
}
