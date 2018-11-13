package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.ContentParseResult.NotApplicable
import de.zalando.zally.rule.ContentParseResult.ParsedSuccessfully
import de.zalando.zally.rule.ContentParseResult.ParsedWithErrors
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.UseOpenApiRule
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException

abstract class RulesValidator<RootT : Any>(val rules: RulesManager) : ApiValidator {

    private val log = LoggerFactory.getLogger(RulesValidator::class.java)
    private val reader = ObjectTreeReader()

    private val useOpenApiRule: RuleDetails by lazy {
        rules.rules.firstOrNull { it.rule.id == UseOpenApiRule.id }
            ?: throw IllegalStateException("""Rule "${UseOpenApiRule::class.simpleName}" must be registered in "${RulesManager::class.simpleName}".""")
    }

    override fun validate(content: String, policy: RulesPolicy): List<Result> {
        val parseResult = parse(content)
        val locator = JsonPointerLocator(content)
        return when (parseResult) {
            is NotApplicable ->
                emptyList()
            is ParsedWithErrors ->
                parseResult.violations.map { violation ->
                    Result(
                        ruleSet = useOpenApiRule.ruleSet,
                        rule = useOpenApiRule.rule,
                        description = violation.description,
                        violationType = useOpenApiRule.rule.severity,
                        pointer = violation.pointer,
                        lines = locator.locate(violation.pointer)
                    )
                }
            is ParsedSuccessfully ->
                rules
                    .checks(policy)
                    .filter { details -> isCheckMethod(details, parseResult.result) }
                    .flatMap { details -> invoke(details, parseResult.result, locator) }
                    .sortedBy(Result::violationType)
        }
    }

    abstract fun parse(content: String): ContentParseResult<RootT>

    private fun isCheckMethod(details: CheckDetails, root: Any) =
        when (details.method.parameters.size) {
            1 -> isRootParameterNeeded(details, root)
            else -> false
        }

    private fun isRootParameterNeeded(details: CheckDetails, root: Any) =
        details.method.parameters.isNotEmpty() &&
            details.method.parameters[0].type.isAssignableFrom(root::class.java)

    private fun invoke(details: CheckDetails, root: RootT, locator: JsonPointerLocator): Iterable<Result> {
        log.debug("validating ${details.method.name} of ${details.instance.javaClass.simpleName} rule")

        val result = try {
            details.method.invoke(details.instance, root)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(
                "check invocation failed: ruleId=${details.rule.id} " +
                    "ruleTitle=${details.rule.title} checkName=${details.method.name} reason=${e.targetException}", e
            )
        }

        val violations = when (result) {
            null -> emptyList()
            is Violation -> listOf(result)
            is Iterable<*> -> result.filterIsInstance(Violation::class.java)
            else -> throw Exception("Unsupported return type for a @Check method!: ${result::class.java}")
        }
        log.debug("${violations.count()} violations identified")

        return violations
            .filterNot {
                ignore(root, it.pointer, details.rule.id)
            }
            .map {
                Result(details.ruleSet, details.rule, it.description, details.check.severity, it.pointer, locator.locate(it.pointer))
            }
    }

    abstract fun ignore(root: RootT, pointer: JsonPointer, ruleId: String): Boolean
}
