package de.zalando.zally.rule.zally

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "M008",
        severity = Severity.MUST,
        title = "Host should not contain protocol"
)
class NoProtocolInHostRule {
    private val desc = "Information about protocol should be placed in schema. Current host value '%s' violates this rule"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val host = swagger.host.orEmpty()
        return if ("://" in host)
            Violation(desc.format(host), emptyList())
        else null
    }
}
