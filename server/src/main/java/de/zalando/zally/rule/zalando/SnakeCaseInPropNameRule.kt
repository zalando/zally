package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.getAllJsonObjects
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "118",
        severity = Severity.MUST,
        title = "snake_case property names"
)
class SnakeCaseInPropNameRule(@Autowired rulesConfig: Config) {
    private val description = "Property names must be snake_case: "

    private val whitelist = rulesConfig.getStringList(SnakeCaseInPropNameRule::class.simpleName + ".whitelist").toSet()

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? {
        if (adapter.isV2()) {
            val swagger = adapter.swagger!!
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
        return Violation.UNSUPPORTED_API_VERSION
    }
}
