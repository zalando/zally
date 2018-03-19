package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import org.slf4j.LoggerFactory

abstract class RulesValidator<RootT>(val rules: RulesManager) : ApiValidator {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)

    private val reader = ObjectTreeReader()

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> {
        val root = parse(content) ?: return emptyList()

        return rules
                .checks(requestPolicy)
                .map { context(root, requestPolicy, it) }
                .filter { it.accepts() }
                .filter { isCheckMethod(it) }
                .flatMap { invoke(it) }
                .sortedBy(Result::violationType)
    }

    abstract fun parse(content: String): RootT?

    /**
     * Build a context for the a particular check.
     * @param root the model root
     * @param policy the rule policy to apply
     * @param details references to rule metadata
     * @return a model specific context
     */
    abstract fun context(root: RootT, policy: RulesPolicy, details: CheckDetails): Context<RootT>

    private fun isCheckMethod(context: Context<RootT>) =
            isRootCheck(context) ||
            isContextCheck(context)

    private fun invoke(context: Context<RootT>): Iterable<Result> {
        val details = context.details
        val root = context.root
        val method = details.method
        val instance = details.instance

        log.debug("validating ${method.name} of ${instance.javaClass.simpleName} rule")

        val result =
                when {
                    isRootCheck(context) ->
                        method.invoke(instance, root)
                    isContextCheck(context) ->
                        method.invoke(instance, context)
                    else -> null
                }

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

    @Suppress("UnsafeCast")
    private fun isRootCheck(context: Context<RootT>) =
            context.details.method.parameterCount == 1 &&
                    context.details.method.parameterTypes[0].isAssignableFrom((context.root as Any)::class.java)

    private fun isContextCheck(context: Context<RootT>) =
            context.details.method.parameterCount == 1 &&
                    context.details.method.parameterTypes[0].isAssignableFrom(context::class.java)
}
