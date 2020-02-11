package de.zalando.zally.ruleset.zally

import de.zalando.zally.core.toJsonPointer
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Context
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZallyRuleSet::class,
    id = "1000",
    severity = Severity.SHOULD,
    title = "Should have 'FIF' on api title"
)

class FIFTitleRule {
    val description = "'FIF' should be on the api title"

    @Check(Severity.SHOULD)
    fun validate(context: Context): Violation? {
        val title = context.api.info?.title.toString()
        if (!(title.contains("FIF"))) {
            return context.violation("No 'FIF' on title", "/info/title".toJsonPointer())
        } else {
            return null
        }
    }
}
