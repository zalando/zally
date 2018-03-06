package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import org.slf4j.LoggerFactory

abstract class RulesValidator<RootT>(val rules: RulesManager) : ApiValidator {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)

    private val reader = ObjectTreeReader()

    val zallyIgnoreExtension = "x-zally-ignore"

    final override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> {
        val root = parse(content) ?: return emptyList()

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
        when (details.method.parameters.size) {
            1 -> isRootParameterNeeded(details, root)
            2 -> isRootParameterNeeded(details, root) && isSwaggerIgnoreExtensionParameterNeeded(details)
            else -> false
        }

    private fun isRootParameterNeeded(details: CheckDetails, root: Any) =
        details.method.parameters.size > 0 &&
        details.method.parameters[0].type.isAssignableFrom(root::class.java)

    private fun isSwaggerIgnoreExtensionParameterNeeded(details: CheckDetails) =
        details.method.parameters.size > 1 &&
        details.method.parameters[1].type.isAssignableFrom(SwaggerIgnoreExtension::class.java)

    private fun invoke(details: CheckDetails, root: RootT): Iterable<Result> {
        log.debug("validating ${details.method.name} of ${details.instance.javaClass.simpleName} rule")

        val result =
            if (isSwaggerIgnoreExtensionParameterNeeded(details)) {
                details.method.invoke(details.instance, root, SwaggerIgnoreExtension(details.rule.id))
            } else {
                details.method.invoke(details.instance, root)
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
}
