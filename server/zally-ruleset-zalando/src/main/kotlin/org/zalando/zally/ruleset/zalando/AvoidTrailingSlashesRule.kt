package org.zalando.zally.ruleset.zalando

import org.zalando.zally.core.plus
import org.zalando.zally.core.toEscapedJsonPointer
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "136",
    severity = Severity.MUST,
    title = "Avoid Trailing Slashes"
)
class AvoidTrailingSlashesRule {
    private val description = "Rule avoid trailing slashes is not followed"

    @Check(severity = Severity.MUST)
    fun validate(context: Context): List<Violation> =
        context.validatePaths(
            pathFilter = { (path, _) ->
                path.trim().let { trimmed ->
                    when {
                        trimmed == "/" -> false
                        trimmed.endsWith("/") -> true
                        else -> false
                    }
                }
            }
        ) { (path, _) ->
            context.violations(description, "/paths".toJsonPointer() + path.toEscapedJsonPointer())
        }
}
