package org.zalando.zally.core

import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.RuleSet
import java.lang.reflect.Method

data class CheckDetails(
    val ruleSet: RuleSet,
    val rule: Rule,
    val instance: Any,
    val check: Check,
    val method: Method
)
