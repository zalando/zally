package com.corefiling.pdds.zally.rule.collections

import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "CollectionsArePlural",
        severity = Severity.SHOULD,
        title = "Collection Resources Are Plural"
)
class CollectionsArePlural : AbstractRule() {
    val description = "Collection resources are plural to indicate that they multiple child resources will be available"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.collections()
                    .map { (pattern, _) ->
                        val lastWord = pattern
                                .split(Regex("\\W"))
                                .last { it.isNotBlank() }

                        if (isPlural(lastWord)) {
                            null
                        } else {
                            "paths $pattern: '$lastWord' appears to be singular"
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }
}