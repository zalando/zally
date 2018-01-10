package com.corefiling.zally.rule.naming

import com.corefiling.pdds.zally.extensions.validateParameter
import com.corefiling.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isCamelCase
import io.swagger.models.Swagger
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "LowerCamelCaseParameterNames",
        severity = Severity.SHOULD,
        title = "Lower Camel Case Parameter Names"
)
class LowerCamelCaseParameterNames : AbstractRule() {
    val description = "Query and path parameters should be named in lowerCamelCase style"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.validateParameter(description) { _, _, _, _, parameter ->
                if (isCamelCase(parameter.name)) null else {
                    when (parameter) {
                        is PathParameter -> "is not lowerCamelCase"
                        is QueryParameter -> "is not lowerCamelCase"
                        else -> null
                    }
                }
            }
}