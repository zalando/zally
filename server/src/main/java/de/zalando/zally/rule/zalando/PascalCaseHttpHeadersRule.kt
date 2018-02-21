package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.HttpHeadersRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "132",
        severity = Severity.SHOULD,
        title = "Prefer Hyphenated-Pascal-Case for HTTP header fields"
)
class PascalCaseHttpHeadersRule(@Autowired rulesConfig: Config) : HttpHeadersRule(rulesConfig) {

    @Check(severity = Severity.SHOULD)
    override fun validate(swagger: Swagger): Violation? {
        return super.validate(swagger)
    }

    override fun isViolation(header: String) = !PatternUtil.isHyphenatedPascalCase(header)

    override fun createViolation(paths: List<String>): Violation {
        return Violation("Header is not Hyphenated-Pascal-Case", paths)
    }
}
