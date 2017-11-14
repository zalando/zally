package de.zalando.zally.rule

abstract class RulesValidator<RuleT, RootT>(val rules: List<RuleT>, val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    private val reader = ObjectTreeReader()

    val zallyIgnoreExtension = "x-zally-ignore"

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Violation> {
        return try {
            val root = parse(content)
            val contentPolicy = requestPolicy.withMoreIgnores(ignores(root))

            rules
                    .filter(contentPolicy::accepts)
                    .flatMap(validator(root))
                    .sortedBy(Violation::violationType)
        } catch (e: Exception) {
            listOf(invalidApiRule.getGeneralViolation())
        }
    }

    @Throws(java.lang.Exception::class)
    abstract fun parse(content: String): RootT

    @Throws(java.lang.Exception::class)
    abstract fun ignores(root: RootT): List<String>

    @Throws(java.lang.Exception::class)
    abstract fun validator(root: RootT): (RuleT) -> Iterable<Violation>
}
