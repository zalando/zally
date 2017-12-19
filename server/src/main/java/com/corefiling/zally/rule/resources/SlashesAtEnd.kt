package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SlashesAtEnd(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Resources Avoid Trailing Slashes"
    override val violationType = ViolationType.SHOULD
    override val description = "Resources should respond the same whether a trailing slash is specified or not"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.endsWith("/")) {
                            pattern
                        } else {
                            null
                        }
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }
}