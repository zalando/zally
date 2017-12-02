package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AvoidLinkHeadersRule(@Autowired ruleSet: ZalandoRuleSet, @Autowired rulesConfig: Config) : HttpHeadersRule(ruleSet, rulesConfig) {
    override val title = "Avoid Link in Header Rule"
    override val id = "166"
    private val DESCRIPTION = "Do Not Use Link Headers with JSON entities"

    @Check(severity = Severity.MUST)
    override fun validate(swagger: Swagger): Violation? {
        return super.validate(swagger)
    }

    override fun isViolation(header: String) = header == "Link"

    override fun createViolation(paths: List<String>): Violation {
        return Violation(DESCRIPTION, paths)
    }
}
