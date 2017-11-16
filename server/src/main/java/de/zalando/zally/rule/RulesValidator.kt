package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import java.lang.reflect.Method

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

    private fun validator(root: Any): (RuleT) -> Iterable<Violation> {
        return { rule: RuleT ->
            rule::class.java.methods
                    .filter { it.isAnnotationPresent(Check::class.java) }
                    .filter { it.parameters.size == 1 }
                    .filter { it.parameters[0].type.isAssignableFrom(root::class.java) }
                    .flatMap { invoke(it, rule, root) }
        }
    }

    private fun invoke(check: Method, rule: RuleT, root: Any): Iterable<Violation> {
        val result = check.invoke(rule, root)
        return when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result as Iterable<Violation>
            else -> throw Exception("Unsupported return type for a @Check check!: ${result::class.java}")
        }
    }
}
