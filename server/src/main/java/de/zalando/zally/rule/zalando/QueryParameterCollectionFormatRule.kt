package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllParameters
import io.swagger.v3.oas.models.parameters.Parameter

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "154",
    severity = Severity.SHOULD,
    title = "Use `form` Style for Query Collection Parameters" //TODO rephrase after guideline update
)
class QueryParameterCollectionFormatRule {
    private val allowedStyle = Parameter.StyleEnum.FORM
    private val description = "Parameter style have to be `form`"

    @Check(severity = Severity.SHOULD)
    fun checkParametersCollectionFormat(context: Context): List<Violation> =
        if (context.isOpenAPI3())
            context.api.getAllParameters().entries
                .filter { "query" == it.value.`in` && "array" == it.value.schema.type }
                .filter { it.value.style == null || allowedStyle != it.value.style }
                .map { context.violation(description, it.value) }
        else emptyList()
}
