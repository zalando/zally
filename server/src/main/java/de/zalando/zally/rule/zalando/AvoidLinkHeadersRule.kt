package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.HttpHeadersRuleWithContext
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "166",
    severity = Severity.MUST,
    title = "Avoid Link in Header Rule"
)
class AvoidLinkHeadersRule(@Autowired rulesConfig: Config) : HttpHeadersRuleWithContext(rulesConfig) {
    private val description = "Do Not Use Link Headers with JSON entities"

    @Check(severity = Severity.MUST)
    override fun validate(context: Context): List<Violation> {
        return super.validate(context)
    }

    override fun isViolation(header: HeaderElement) = header.name == "Link"

    override fun createViolation(context: Context, header: HeaderElement): Violation =
        context.violation(description, header.element)
}
