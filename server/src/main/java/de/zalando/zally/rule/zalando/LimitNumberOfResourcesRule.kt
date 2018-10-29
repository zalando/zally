package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil.isPathVariable
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "146",
    severity = Severity.SHOULD,
    title = "Limit number of resource types"
)
class LimitNumberOfResourcesRule(@Autowired rulesConfig: Config) {
    private val resourceTypesLimit = rulesConfig.getConfig(javaClass.simpleName).getInt("resource_types_limit")

    @Check(severity = Severity.SHOULD)
    fun checkLimitOfResources(context: Context): Violation? {
        val resourceTypes = resourceTypes(context.api.paths.orEmpty().keys)
        return if (resourceTypes.size > resourceTypesLimit) {
            context.violation(
                "Identified ${resourceTypes.size} resource resource types, " +
                    "greater than recommended limit of $resourceTypesLimit",
                context.api.paths
            )
        } else null
    }

    internal fun resourceTypes(paths: Collection<String>): List<String> {
        return paths.map(this::resourceType).distinct()
    }

    internal fun resourceType(path: String): String {
        val components = path
            .split(Regex("/+"))
            .filter(String::isNotEmpty)
        val size = components.size

        return when {
            size > 0 && isPathVariable(components[size - 1]) ->
                components.dropLast(1)
            size > 1 && isPathVariable(components[size - 2]) ->
                components.dropLast(2)
            else ->
                components
        }.joinToString(prefix = "/", separator = "/")
    }
}
