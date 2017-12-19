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
class SlashesAtStart(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Resources Start with /"
    override val violationType = ViolationType.MUST
    override val description = "Resources pattern starts with a /"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.startsWith("/")) {
                            null
                        } else {
                            pattern
                        }
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }
}