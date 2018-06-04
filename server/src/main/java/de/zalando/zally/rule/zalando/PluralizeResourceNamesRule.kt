package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
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
    fun validate(swagger: Swagger): Violation? {
        val res = swagger.paths?.keys?.flatMap { path ->
            val allParts = path.split("/".toRegex())
            val partsToCheck = if (allParts.size > 1 && allowedPrefixes.contains(allParts.first())) allParts.drop(1)
            else allParts

            partsToCheck.filter { s -> !s.isEmpty() && !PatternUtil.isPathVariable(s) && !isPlural(s) }
                .map { it to path }
        }
        return if (res != null && res.isNotEmpty()) {
            val desc = res.map { "'${it.first}'" }.toSet().joinToString(", ")
            val paths = res.map { it.second }
            Violation(String.format(description, desc), paths)
        } else null
    }
}
