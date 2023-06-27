package org.zalando.zally.ruleset.sbb

import com.fasterxml.jackson.core.JsonPointer
import com.typesafe.config.Config
import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.PatternUtil
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation
import org.zalando.zally.ruleset.zalando.util.WordUtil.isPlural

@Rule(
    ruleSet = SBBRuleSet::class,
    id = "restful/best-practices/#resources",
    severity = Severity.SHOULD,
    title = "Pluralize Resource Names"
)
class PluralizeResourceNamesRule(rulesConfig: Config) {

    private val slash = "/"

    private val slashes = "/+".toRegex()
    private val semVerVersionFormat = "^[0-9]+([.][0-9]+){1,2}$".toRegex()

    @Suppress("SpreadOperator")
    internal val whitelist = mutableListOf(
        *rulesConfig
            .getConfig(javaClass.simpleName)
            .getStringList("whitelist")
            .map { it.toRegex() }
            .toTypedArray()
    )

    @Check(severity = Severity.SHOULD)
    fun validate(context: Context): List<Violation> {
        return context.validatePaths { (path, _) ->
            pathSegments(sanitizedPath(path, whitelist))
                .filter { isNonViolating(it) }
                .map { violation(context, it, "/paths".toJsonPointer() + path.toEscapedJsonPointer()) }
        }
    }

    private fun sanitizedPath(path: String, regexList: List<Regex>): String {
        return regexList.fold("/$path/".replace(slashes, slash)) { updated, regex ->
            updated.replace(regex, slash)
        }
    }

    private fun pathSegments(path: String): List<String> {
        return path.split(slashes).filter { it.isNotEmpty() }
    }

    private fun isNonViolating(it: String) =
        (!PatternUtil.isPathVariable(it) && (!isPlural(it) || it.contains(".")))

    private fun violation(context: Context, term: String, pointer: JsonPointer) =
        when {
            semVerVersionFormat.matches(term) -> {
                context.violation("Resource '$term' is a version, instead of a resource", pointer)
            }
            term.contains(".") -> {
                context.violation("Resource '$term' has a dot as delimiter", pointer)
            }
            else -> {
                context.violation("Resource '$term' appears to be singular", pointer)
            }
        }
}
