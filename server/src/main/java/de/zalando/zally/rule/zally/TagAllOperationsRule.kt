package de.zalando.zally.rule.zally

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
}
