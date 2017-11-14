package de.zalando.zally.rule

abstract class RulesValidator<RuleT, RootT>(val rules: List<RuleT>, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    private val reader = ObjectTreeReader()

    val zallyIgnoreExtension = "x-zally-ignore"

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Violation> {
        val root = parse(content) ?: return listOf(invalidApiRule.getGeneralViolation())

        val contentPolicy = requestPolicy.withMoreIgnores(ignores(root))

        return rules
                .filter(contentPolicy::accepts)
                .flatMap(validator(root))
                .sortedBy(Violation::violationType)
    }

    abstract fun parse(content: String): RootT?

    abstract fun ignores(root: RootT): List<String>

    abstract fun validator(root: RootT): (RuleT) -> Iterable<Violation>
}
