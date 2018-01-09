package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "SlashesAtEnd",
        severity = Severity.SHOULD,
        title = "Resources Avoid Trailing Slashes"
)
class SlashesAtEnd : AbstractRule() {
    val description = "Resources should respond the same whether a trailing slash is specified or not"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.endsWith("/")) {
                            pattern
                        } else {
                            null
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }
}