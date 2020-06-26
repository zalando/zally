package de.zalando.zally.ruleset.sbb

import com.typesafe.config.Config
import de.zalando.zally.core.CaseChecker
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#property-names-must-be-camelcase",
    severity = Severity.MUST,
    title = "Property Names Must be ASCII camelCase"
)
class CamelCaseInPropNameRule(config: Config) {
    private val description = "Property name has to be camelCase"

    private val checker = CaseChecker.load(config)

    @Check(severity = Severity.MUST)
    fun checkPropertyNames(context: Context): List<Violation> =
        checker.checkPropertyNames(context).map { Violation(description, it.pointer) }
}
