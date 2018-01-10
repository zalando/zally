package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import com.corefiling.zally.rule.collections.pathParamRegex
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PathParamIsWholePathComponent",
        severity = Severity.MUST,
        title = "Path Parameters Are Entire Path Components"
)
class PathParamIsWholePathComponent : AbstractRule() {
    val description = "Path parameters occupy an entire path component between slashes, never a partial component"

    @Check(Severity.MUST)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        pattern
                                .split('/')
                                .filter { pathParamRegex.find(it) != null }
                                .filter { pathParamRegex.replaceFirst(it, "XXXXX") != "XXXXX" }
                                .ifNotEmptyLet { "$pattern contains partial component path parameters: ${it.joinToString()}" }
                    }
                    .ifNotEmptyLet { Violation(description, it) }
}