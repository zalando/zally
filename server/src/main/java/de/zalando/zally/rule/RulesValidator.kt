package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import org.slf4j.LoggerFactory

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
                .checks(contentPolicy)
                .filter { details -> isCheckMethod(details, root) }
                .flatMap { details -> invoke(details, root) }
                .sortedBy(Result::violationType)
    }

    abstract fun parse(content: String): RootT?

    abstract fun ignores(root: RootT): List<String>

    private fun isCheckMethod(details: CheckDetails, root: Any) =
                details.method.parameters.size == 1 &&
                details.method.parameters[0].type.isAssignableFrom(root::class.java)

    private fun invoke(details: CheckDetails, root: Any): Iterable<Result> {
        log.debug("validating ${details.method.name} of ${details.instance.javaClass.simpleName} rule")
        val result = details.method.invoke(details.instance, root)
        val violations = when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result as Iterable<Violation>
            else -> throw Exception("Unsupported return type for a @Check method!: ${result::class.java}")
        }
        log.debug("${violations.count()} violations identified")
        return violations
                .map { Result(details.ruleSet, details.rule, it.description, details.check.severity, it.paths) }
    }
}
