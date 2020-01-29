package de.zalando.zally.core

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import java.lang.reflect.Method

data class CheckDetails(
    val ruleSet: RuleSet,
    val rule: Rule,
    val instance: Any,
    val check: Check,
    val method: Method
)
