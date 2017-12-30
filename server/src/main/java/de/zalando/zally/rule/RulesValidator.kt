package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import org.slf4j.LoggerFactory
import java.lang.reflect.Method

abstract class RulesValidator<RootT>(val rules: RulesManager, private val invalidApiRule: InvalidApiSchemaRule) : ApiValidator {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)

    private val reader = ObjectTreeReader()

    val zallyIgnoreExtension = "x-zally-ignore"

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> {
        val root = parse(content) ?: return listOf(invalidApiRule.getGeneralViolation())

        val moreIgnores = ignores(root)
        log.debug("ignoring $moreIgnores from document as well as ${requestPolicy.ignoreRules}")

        val contentPolicy = requestPolicy.withMoreIgnores(moreIgnores)

        return rules
                .rules(contentPolicy)
                .flatMap(validator(root))
                .sortedBy(Result::violationType)
    }

    abstract fun parse(content: String): RootT?

    abstract fun ignores(root: RootT): List<String>

    private fun validator(root: Any): (RuleDetails) -> Iterable<Result> {
        return { details: RuleDetails ->
            log.debug("validating ${details.instance.javaClass.simpleName} rule")
            details.instance::class.java.methods
                    .filter { isCheckMethod(it, root) }
                    .flatMap { invoke(it, details.instance, root) }
        }
    }

    private fun isCheckMethod(it: Method, root: Any) =
            it.isAnnotationPresent(Check::class.java) &&
                    it.parameters.size == 1 &&
                    it.parameters[0].type.isAssignableFrom(root::class.java)

    private fun invoke(method: Method, rule: Rule, root: Any): Iterable<Result> {
        log.debug("validating ${method.name} of ${rule.javaClass.simpleName} rule")
        val check = method.getAnnotation(Check::class.java)
        val result = method.invoke(rule, root)
        val violations = when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result as Iterable<Violation>
            else -> throw Exception("Unsupported return type for a @Check method!: ${result::class.java}")
        }
        log.debug("${violations.count()} violations identified")
        return violations
                .map { Result(rule, rule.title, it.description, check.severity, it.paths) }
    }
}
