package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SnakeCaseInPropNameRule(@Autowired ruleSet: ZalandoRuleSet, @Autowired rulesConfig: Config) : AbstractRule(ruleSet) {
    override val title = "snake_case property names"
    override val id = "118"
    private val description = "Property names must be snake_case: "

    private val whitelist = rulesConfig.getStringList(SnakeCaseInPropNameRule::class.simpleName + ".whitelist").toSet()

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val result = swagger.getAllJsonObjects().flatMap { (def, path) ->
            val badProps = def.keys.filterNot { PatternUtil.isSnakeCase(it) || whitelist.contains(it) }
            if (badProps.isNotEmpty()) listOf(badProps to path) else emptyList()
        }
        return if (result.isNotEmpty()) {
            val (props, paths) = result.unzip()
            val properties = props.flatten().toSet().joinToString(", ")
            Violation(description + properties, paths)
        } else null
    }
}
