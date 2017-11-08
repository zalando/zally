package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LimitNumberOfSubresourcesRule(@Autowired ruleSet: ZalandoRuleSet, @Autowired rulesConfig: Config) : SwaggerRule(ruleSet) {
    override val title = "Limit number of Sub-resources level"
    override val url = "/#147"
    override val violationType = ViolationType.SHOULD
    override val code = "S003"
    override val guidelinesCode = "147"
    private val DESC = "Number of sub-resources should not exceed 3"
    private val subresourcesLimit = rulesConfig.getConfig(name).getInt("subresources_limit")

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filter { path ->
            path.split("/").filter { it.isNotEmpty() && !PatternUtil.isPathVariable(it) }.size - 1 > subresourcesLimit
        }
        return if (paths.isNotEmpty()) Violation(this, title, DESC, violationType, url, paths) else null
    }
}
