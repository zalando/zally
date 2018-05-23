package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "147",
        severity = Severity.SHOULD,
        title = "Limit number of Sub-resources level"
)
class LimitNumberOfSubresourcesRule(@Autowired rulesConfig: Config) {
    private val description = "Number of sub-resources should not exceed 3"
    private val subresourcesLimit = rulesConfig.getConfig(javaClass.simpleName).getInt("subresources_limit")

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {

        val paths = swagger.paths.orEmpty()
//                .filterValues { ignore.accepts(it.vendorExtensions) } FIXME
                .keys.filter { path ->
            path.split("/").filter { it.isNotEmpty() && !PatternUtil.isPathVariable(it) }.size - 1 > subresourcesLimit
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }
}
