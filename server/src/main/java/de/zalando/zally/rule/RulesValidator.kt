package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import org.slf4j.LoggerFactory

abstract class RulesValidator<RootT>(val rules: RulesManager) : ApiValidator {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)
    private val reader = ObjectTreeReader()

    override fun validate(content: String, policy: RulesPolicy): List<Result> {
        val root = parse(content) ?: return emptyList()

        return rules
            .checks(policy)
            .filter { details -> isCheckMethod(details, root) }
            .flatMap { details -> invoke(details, root) }
            .sortedBy(Result::violationType)
    }

    abstract fun parse(content: String): RootT?

    private fun isCheckMethod(details: CheckDetails, root: Any) =
        when (details.method.parameters.size) {
            1 -> isRootParameterNeeded(details, root)
            else -> false
        }

    private fun isRootParameterNeeded(details: CheckDetails, root: Any) =
        details.method.parameters.isNotEmpty() &&
            details.method.parameters[0].type.isAssignableFrom(root::class.java)

    private fun invoke(details: CheckDetails, root: RootT): Iterable<Result> {
        log.debug("validating ${details.method.name} of ${details.instance.javaClass.simpleName} rule")

        val result = details.method.invoke(details.instance, root)

        val violations = when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result.filterIsInstance(Violation::class.java)
            else -> throw Exception("Unsupported return type for a @Check method!: ${result::class.java}")
        }
        log.debug("${violations.count()} violations identified")

        // TODO: make pointer not-null and remove usage of `paths`
        return violations
            .filterNot {
                ignore(root, it.pointer ?: it.paths.firstOrNull() ?: "#", details.rule.id)
            }
            .map {
                if (it.pointer != null) {
                    Result(details.ruleSet, details.rule, it.description, details.check.severity, it.pointer)
                } else {
                    Result(details.ruleSet, details.rule, it.description, details.check.severity, it.paths)
                }
            }
    }

    abstract fun ignore(root: RootT, pointer: String, ruleId: String): Boolean
}
