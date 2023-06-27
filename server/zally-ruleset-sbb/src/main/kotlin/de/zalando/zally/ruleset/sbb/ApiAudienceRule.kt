package de.zalando.zally.ruleset.sbb

import com.typesafe.config.Config
import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "219",
    severity = Severity.MUST,
    title = "Provide API Audience"
)
class ApiAudienceRule(rulesConfig: Config) {
    private val validAudiences = rulesConfig.getStringList("${javaClass.simpleName}.audiences").toSet()

    private val noApiAudienceDesc = "API Audience must be provided"
    private val invalidApiAudienceDesc = "API Audience doesn't match $validAudiences"
    private val extensionName = "x-audience"
    private val extensionPointer = "/info/$extensionName".toJsonPointer()

    @Check(severity = Severity.MUST)
    fun validate(context: Context): Violation? {
        val audience = context.api.info?.extensions?.get(extensionName)

        return when (audience) {
            null, !is String -> context.violation(noApiAudienceDesc, extensionPointer)
            !in validAudiences -> context.violation(invalidApiAudienceDesc, extensionPointer)
            else -> null
        }
    }
}
