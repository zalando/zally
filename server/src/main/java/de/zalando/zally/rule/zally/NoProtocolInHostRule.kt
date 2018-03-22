package de.zalando.zally.rule.zally

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "M008",
        severity = Severity.MUST,
        title = "Host should not contain protocol"
)
class NoProtocolInHostRule {
    private val desc = "Information about protocol should be placed in schema. Current host value '%s' violates this rule"

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger
            val host = swagger?.host.orEmpty()
            return if ("://" in host)
                Violation(desc.format(host), emptyList())
            else null
        }
        return null
    }
}
