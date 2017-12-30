package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet
import java.lang.reflect.Method

data class RuleDetails(
        val ruleSet: RuleSet,
        val instance: Rule
) {
    fun toCheckDetails(check: Check, method: Method): CheckDetails = CheckDetails(ruleSet, instance, check, method)
}
