package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RulesPolicy(
        @Value("\${zally.ignoreRules:}") val ignoreRules: Array<String>,
        @Value("\${zally.ignoreRulePackages:}") val ignoreRulePackages: Array<String>
) {
    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.code) && !ignoreRulePackages.contains(rule.javaClass.`package`.name)
    }
}
