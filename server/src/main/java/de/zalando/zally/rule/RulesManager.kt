package de.zalando.zally.rule

import org.springframework.stereotype.Component

@Component
class RulesManager(val rules: List<RuleDetails>) {

    fun rules(policy: RulesPolicy): List<RuleDetails> {
        return rules.filter { details -> policy.accepts(details.instance) }
    }

    fun size(): Int = rules.size
}
