package com.corefiling.pdds.zally.rule.resources

import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import com.corefiling.pdds.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "SlashesAtStart",
        severity = Severity.MUST,
        title = "Resources Start with /"
)
class SlashesAtStart {
    val description = "Resources pattern starts with a /"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.startsWith("/")) {
                            null
                        } else {
                            pattern
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }
}