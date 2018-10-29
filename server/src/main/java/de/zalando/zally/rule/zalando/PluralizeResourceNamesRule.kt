package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.v3.oas.models.PathItem
import org.springframework.beans.factory.annotation.Autowired

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "134",
    severity = Severity.MUST,
    title = "Pluralize Resource Names"
)
class PluralizeResourceNamesRule(@Autowired rulesConfig: Config) {

    private val slash = "/"

    private val slashes = "/+".toRegex()

    @Suppress("SpreadOperator")
    internal val whitelist = mutableListOf(
        *rulesConfig
            .getConfig(javaClass.simpleName)
            .getStringList("whitelist")
            .map { it.toRegex() }
            .toTypedArray())

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> {
        return context.validatePaths { (path, definition) ->
            pathSegments(sanitizedPath(path, whitelist))
                .filter { isNonViolating(it) }
                .map { violation(context, it, definition) }
        }
    }

    private fun sanitizedPath(path: String, regexList: List<Regex>): String {
        return regexList.fold("/$path/".replace(slashes, slash)) { updated, regex ->
            updated.replace(regex, slash)
        }
    }

    private fun pathSegments(path: String): List<String> {
        return path.split(slashes).filter { !it.isEmpty() }
    }

    private fun isNonViolating(it: String) =
        !PatternUtil.isPathVariable(it) && !isPlural(it)

    private fun violation(context: Context, term: String, definition: PathItem?) =
        context.violation("Resource '$term' appears to be singular", definition)
}
