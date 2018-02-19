package de.zalando.zally.rule.zally

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.HttpHeadersRule
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZallyRuleSet::class,
        id = "131",
        severity = Severity.MUST,
        title = "Use Hyphenated HTTP Headers"
)
class HyphenateHttpHeadersRule(@Autowired rulesConfig: Config) : HttpHeadersRule(rulesConfig) {

    @Check(severity = Severity.MUST)
    override fun validate(swagger: Swagger): Violation? {
        return super.validate(swagger)
    }

    override fun isViolation(header: String) = !PatternUtil.isHyphenated(header)

    override fun createViolation(paths: List<String>): Violation {
        return Violation("Header names should be hyphenated", paths)
    }
}
