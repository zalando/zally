package de.zalando.zally.ruleset.sbb

import de.zalando.zally.core.util.getAllParameters
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.parameters.Parameter

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "154",
    severity = Severity.SHOULD,
    title = "Use and Specify Explicitly the Form-Style Query Format for Collection Parameters"
)
class QueryParameterCollectionFormatRule {
    private val allowedStyle = Parameter.StyleEnum.FORM
    private val description = "Parameter style have to be `form`"

    @Check(severity = Severity.SHOULD)
    fun checkParametersCollectionFormat(context: Context): List<Violation> =
        if (context.isOpenAPI3())
            context.api.getAllParameters().values
                .filter { "query" == it.`in` && "array" == it.schema?.type }
                .filter { it.style == null || allowedStyle != it.style }
                .map { context.violation(description, it) }
        else emptyList()
}
