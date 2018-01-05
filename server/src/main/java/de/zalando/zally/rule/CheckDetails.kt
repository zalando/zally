package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.RuleSet
import de.zalando.zally.rule.api.Rule
import java.lang.reflect.Method

data class CheckDetails(
        val ruleSet: RuleSet,
        val rule: Rule,
        val instance: Any,
        val check: Check,
        val method: Method
)
