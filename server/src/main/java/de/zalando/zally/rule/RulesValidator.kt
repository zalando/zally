package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

abstract class RulesValidator<out RuleT, RootT>(val rules: List<RuleT>, private val invalidApiRule: InvalidApiSchemaRule) : ApiValidator where RuleT : Rule {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)

    private val reader = ObjectTreeReader()

    val zallyIgnoreExtension = "x-zally-ignore"

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> {
        val root = parse(content) ?: return listOf(invalidApiRule.getGeneralViolation())

        val moreIgnores = ignores(root)
        log.debug("ignoring $moreIgnores from document as well as ${requestPolicy.ignoreRules}")

        val contentPolicy = requestPolicy.withMoreIgnores(moreIgnores)

        return rules
                .filter(contentPolicy::accepts)
                .flatMap(validator(root))
                .sortedBy(Result::violationType)
    }

    abstract fun parse(content: String): RootT?

    abstract fun ignores(root: RootT): List<String>

    private fun validator(root: Any): (RuleT) -> Iterable<Result> {
        return { rule: RuleT ->
            log.debug("validating ${rule.javaClass.simpleName} rule")
            rule::class.java.methods
                    .filter { isCheckMethod(it, root) }
                    .flatMap { invoke(it, rule, root) }
        }
    }

    private fun isCheckMethod(it: Method, root: Any) =
            it.isAnnotationPresent(Check::class.java) &&
                    it.parameters.size == 1 &&
                    it.parameters[0].type.isAssignableFrom(root::class.java)

    private fun invoke(check: Method, rule: RuleT, root: Any): Iterable<Result> {
        log.debug("validating ${check.name} of ${rule.javaClass.simpleName} rule")
        val result = check.invoke(rule, root)
        val violations = when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result as Iterable<Violation>
            else -> throw Exception("Unsupported return type for a @Check check!: ${result::class.java}")
        }
        log.debug("${violations.count()} violations identified")
        return violations
                .map { Result(rule, rule.title, it.description, it.violationType, it.paths) }
    }
}
