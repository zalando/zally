package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllProperties
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "118",
    severity = Severity.MUST,
    title = "Property Names Must be ASCII snake_case"
)
class SnakeCaseInPropNameRule(@Autowired rulesConfig: Config) {
    private val description = "Property name has to be snake_case"

    private val whitelist = rulesConfig.getStringList(SnakeCaseInPropNameRule::class.simpleName + ".whitelist").toSet()

    @Check(severity = Severity.MUST)
    fun checkPropertyNames(context: Context): List<Violation> =
        context.api.getAllProperties()
            .filterNot { (name, _) -> PatternUtil.isSnakeCase(name) || whitelist.contains(name) }
            .map { context.violation(description, it.value) }
}
