package com.corefiling.pdds.zally.rule.resources

import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import com.corefiling.pdds.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "SlashesNotDoubled",
        severity = Severity.SHOULD,
        title = "Resources Separated by Single /"
)
class SlashesNotDoubled : AbstractRule() {
    val description = "Resources pattern separated by single slashes, not //"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.contains("//")) {
                            pattern
                        } else {
                            null
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }
}
