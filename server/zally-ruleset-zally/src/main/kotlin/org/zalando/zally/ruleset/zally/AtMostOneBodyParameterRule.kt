package org.zalando.zally.ruleset.zally

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import io.swagger.models.parameters.BodyParameter

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "M009",
    severity = Severity.MUST,
    title = "At Most One Body Parameter"
)
class AtMostOneBodyParameterRule {
    val description = "No more than one body parameter can be associated with an operation"

    @Check(Severity.MUST)
    fun validate(context: Context): List<Violation> = context.swagger?.paths
        .orEmpty().flatMap { (_, path) ->
            path.operations.flatMap { op ->
                op.parameters.orEmpty().filter { it is BodyParameter }.let { bodies ->
                    if (bodies.size > 1) {
                        bodies.map { context.violation("There can only be one body parameter", it) }
                    } else {
                        emptyList()
                    }
                }
            }
        }
}
