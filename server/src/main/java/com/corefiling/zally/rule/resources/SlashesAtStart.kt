package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SlashesAtStart(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Resources Start with /"
    override val violationType = ViolationType.MUST
    override val description = "Resources pattern starts with a /"

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        swagger.paths?.forEach { pattern, _ ->
            if (!pattern.startsWith("/")) {
                failures.add(pattern)
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }
}