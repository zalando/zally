package de.zalando.zally.rule.zally

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.zalando.HttpHeadersRule
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HyphenateHttpHeadersRule(@Autowired ruleSet: ZallyRuleSet, @Autowired rulesConfig: Config) : HttpHeadersRule(ruleSet, rulesConfig) {
    override val title = "Use Hyphenated HTTP Headers"
    override val id = "131"
    override val severity = Severity.MUST

    @Check(severity = Severity.MUST)
    override fun validate(swagger: Swagger): Violation? {
        return super.validate(swagger)
    }

    override fun isViolation(header: String) = !PatternUtil.isHyphenated(header)

    override fun createViolation(paths: List<String>): Violation {
        return Violation("Header names should be hyphenated", paths)
    }
}
