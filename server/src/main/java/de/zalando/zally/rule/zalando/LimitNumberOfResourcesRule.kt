package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "146",
        severity = Severity.SHOULD,
        title = "Limit number of Resources"
)
class LimitNumberOfResourcesRule(@Autowired rulesConfig: Config) : AbstractRule() {
    private val pathCountLimit = rulesConfig.getConfig(name).getInt("paths_count_limit")

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty()
        val pathsCount = paths.size
        return if (pathsCount > pathCountLimit) {
            Violation("Number of paths $pathsCount is greater than $pathCountLimit",
                    paths.keys.toList())
        } else null
    }
}
