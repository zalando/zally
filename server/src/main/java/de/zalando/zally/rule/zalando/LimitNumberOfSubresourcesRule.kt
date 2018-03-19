package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.SwaggerContext
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "147",
        severity = Severity.SHOULD,
        title = "Limit number of Sub-resources level"
)
class LimitNumberOfSubresourcesRule(@Autowired rulesConfig: Config) {
    private val description = "Number of sub-resources should not exceed 3"
    private val subResourcesLimit = rulesConfig
            .getConfig(javaClass.simpleName)
            .getInt("subresources_limit")

    /**
     * Validate the model using the specified context
     * @param context the context to validate with
     * @return Violation if messages are produced
     */
    @Check(severity = Severity.SHOULD)
    fun validate(context: SwaggerContext): Violation? =
            context.validatePaths(description) { _, pattern, _ ->
                val subResourceCount = pattern.split("/").filter {
                    it.isNotEmpty() && !PatternUtil.isPathVariable(it)
                }.size - 1

                if (subResourceCount > subResourcesLimit) listOf("$subResourceCount sub-resources")
                else emptyList()
            }
}
