package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.WordUtil.isPlural
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "134",
        severity = Severity.MUST,
        title = "Pluralize Resource Names"
)
class PluralizeResourceNamesRule(@Autowired rulesConfig: Config) {
    private val description = "Resource '%s' appears to be singular (but we are not sure)"
    private val allowedPrefixes = rulesConfig.getConfig(javaClass.simpleName).getStringList("whitelist_prefixes")

    @Check(severity = Severity.MUST)
    fun validate2(context: Context): List<Violation> {
        return context.validatePaths { (path, definition) ->

            val allParts = path.split("/".toRegex())
            val partsToCheck = if (allParts.size > 1 && allowedPrefixes.contains(allParts.first())) allParts.drop(1)
            else allParts

            partsToCheck.filter { s -> !s.isEmpty() && !PatternUtil.isPathVariable(s) && !isPlural(s) }
                    .map { context.violation(String.format(description, it), definition) }
        }
    }
}
