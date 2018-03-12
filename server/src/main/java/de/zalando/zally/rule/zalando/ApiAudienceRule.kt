package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "219",
    severity = Severity.MUST,
    title = "Provide API Audience"
)
class ApiAudienceRule(@Autowired rulesConfig: Config) {
    private val validAudiences = rulesConfig.getStringList("${javaClass.simpleName}.audiences").toSet()

    private val noApiAudienceDesc = "API Audience must be provided"
    private val invalidApiAudienceDesc = "API Audience doesn't match $validAudiences"

    private val extensionName = "x-audience"
    private val path = "/info/$extensionName"

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val audience = swagger.info?.vendorExtensions?.get(extensionName)

        return if (audience == null || audience !is String) {
            Violation(noApiAudienceDesc, listOf(path))
        } else {
            if (validAudiences.contains(audience)) null else Violation(invalidApiAudienceDesc, listOf(path))
        }
    }
}