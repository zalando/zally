package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LimitNumberOfResourcesRule(@Autowired ruleSet: ZalandoRuleSet, @Autowired rulesConfig: Config) : AbstractRule(ruleSet) {
    override val title = "Limit number of Resources"
    override val id = "146"
    private val pathCountLimit = rulesConfig.getConfig(name).getInt("paths_count_limit")

    @Check(severity = ViolationType.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty()
        val pathsCount = paths.size
        return if (pathsCount > pathCountLimit) {
            Violation("Number of paths $pathsCount is greater than $pathCountLimit",
                    paths.keys.toList())
        } else null
    }
}
