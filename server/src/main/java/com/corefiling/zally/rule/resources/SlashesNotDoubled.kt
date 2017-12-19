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
class SlashesNotDoubled(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Resources Separated by Single /"
    override val violationType = ViolationType.MUST
    override val description = "Resources pattern separated by single slashes, not //"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        if (pattern.contains("//")) {
                            pattern
                        } else {
                            null
                        }
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }
}
