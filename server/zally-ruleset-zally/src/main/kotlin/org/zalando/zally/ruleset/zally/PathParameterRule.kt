package org.zalando.zally.ruleset.zally

import org.zalando.zally.core.util.getAllParameters
import org.zalando.zally.core.util.isInPath
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "Z001",
    severity = Severity.MUST,
    title = "Path parameters must have 'required' attribute"
)
class PathParameterRule {

    companion object {
        const val ERROR_MESSAGE = "Parameter with location \"path\" must have an attribure \"required=true\" set"
    }

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> =
        context.api.getAllParameters().map { entry -> entry.value }
            .filter { parameter ->
                parameter.isInPath() || !parameter.required
            }
            .map {
                context.violation(ERROR_MESSAGE, it)
            }
}
