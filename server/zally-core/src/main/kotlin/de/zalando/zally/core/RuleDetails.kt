package de.zalando.zally.core

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import java.lang.reflect.Method

data class RuleDetails(
    val ruleSet: RuleSet,
    val rule: Rule,
    val instance: Any
) {
    fun toCheckDetails(check: Check, method: Method): CheckDetails =
        CheckDetails(ruleSet, rule, instance, check, method)
}
