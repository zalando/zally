package de.zalando.zally.rule.zally

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NoProtocolInHostRule(@Autowired ruleSet: ZallyRuleSet) : AbstractRule(ruleSet) {
    override val title = "Host should not contain protocol"
    override val violationType = ViolationType.MUST
    override val id = "M008"
    private val desc = "Information about protocol should be placed in schema. Current host value '%s' violates this rule"

    @Check
    fun validate(swagger: Swagger): Violation? {
        val host = swagger.host.orEmpty()
        return if ("://" in host)
            Violation(this, title, desc.format(host), violationType, emptyList())
        else null
    }
}
