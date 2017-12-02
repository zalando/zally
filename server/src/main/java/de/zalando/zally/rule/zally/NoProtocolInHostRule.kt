package de.zalando.zally.rule.zally

import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NoProtocolInHostRule(@Autowired ruleSet: ZallyRuleSet) : AbstractRule(ruleSet) {
    override val title = "Host should not contain protocol"
    override val id = "M008"
    private val desc = "Information about protocol should be placed in schema. Current host value '%s' violates this rule"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val host = swagger.host.orEmpty()
        return if ("://" in host)
            Violation(desc.format(host), emptyList())
        else null
    }
}
