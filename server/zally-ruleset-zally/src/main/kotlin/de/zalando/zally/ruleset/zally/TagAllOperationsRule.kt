package de.zalando.zally.ruleset.zally

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "M011",
    severity = Severity.MUST,
    title = "Tag all operations"
)
class TagAllOperationsRule {

    @Check(severity = Severity.MUST)
    fun checkOperationsAreTagged(context: Context): List<Violation> =
        context.validateOperations { (_, operation) ->
            when {
                operation == null -> emptyList()
                operation.tags.orEmpty().isEmpty() -> context.violations("Operation has no tag", operation)
                else -> emptyList<Violation>()
            }
        }

    @Check(severity = Severity.MUST)
    fun checkOperationTagsAreDefined(context: Context): List<Violation> {
        val defined = context.api.tags
            .orEmpty()
            .map { it.name }
            .toSet()

        return context.validateOperations { (_, operation) ->
            operation?.tags.orEmpty().filter { it !in defined }.flatMap {
                context.violations("Tag '$it' is not defined", operation!!)
            }
        }
    }

    @Check(severity = Severity.MUST)
    fun checkDefinedTagsAreUsed(context: Context): List<Violation> {
        val used = context.api.paths?.values
            .orEmpty()
            .flatMap { it.readOperations() }
            .flatMap { it?.tags.orEmpty() }
            .toSet()

        return context.api.tags
            .orEmpty()
            .filter { it.name !in used }
            .map {
                context.violation("Tag '${it.name}' is not used", it)
            }
    }

    @Check(severity = Severity.MUST)
    fun checkDefinedTagsAreNamed(context: Context): List<Violation> = context.api.tags
        .orEmpty()
        .filter { it.name == null }
        .map {
            context.violation("Tag has no name", it)
        }

    @Check(severity = Severity.MUST)
    fun checkDefinedTagsAreDescribed(context: Context): List<Violation> = context.api.tags
        .orEmpty()
        .filter { it.description == null }
        .map {
            context.violation("Tag '${it.name}' has no description", it)
        }
}
