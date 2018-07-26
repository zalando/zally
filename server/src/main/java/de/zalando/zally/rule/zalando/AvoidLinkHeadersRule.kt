package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.HttpHeadersRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllHeaders
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "166",
    severity = Severity.MUST,
    title = "Avoid Link in Header Rule"
)
class AvoidLinkHeadersRule(@Autowired rulesConfig: Config) {

    private val headersWhitelist = rulesConfig.getStringList(HttpHeadersRule::class.simpleName + ".whitelist").toSet()

    private val description = "Do Not Use Link Headers with JSON entities"

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        val allHeaders = context.api.getAllHeaders()
        return allHeaders
            .filter { it.name !in headersWhitelist && it.name == "Link" }
            .map { context.violation(description, it.element) } // createViolation(context, it) }
    }
}
