package org.zalando.zally.ruleset.sbb

import io.swagger.v3.oas.models.parameters.Parameter
import org.zalando.zally.core.util.getAllParameters
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#define-collection-format-of-header-and-query-parameters",
    severity = Severity.SHOULD,
    title = "Use and Specify Explicitly the Form-Style Query Format for Collection Parameters"
)
class QueryParameterCollectionFormatRule {
    private val allowedStyle = Parameter.StyleEnum.FORM
    private val description = "Parameter style have to be `form`"

    @Check(severity = Severity.SHOULD)
    fun checkParametersCollectionFormat(context: Context): List<Violation> =
        if (context.isOpenAPI3()) {
            context.api.getAllParameters()
                .filter { "query" == it.`in` && "array" == it.schema?.type }
                .filter { it.style == null || allowedStyle != it.style }
                .map { context.violation(description, it) }
        } else {
            emptyList()
        }
}
