package de.zalando.zally.rule.zalando

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.core.JsonPointers

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
            context.violations(description, JsonPointer.compile("/paths").append(JsonPointers.escape(path)))
        }
}
