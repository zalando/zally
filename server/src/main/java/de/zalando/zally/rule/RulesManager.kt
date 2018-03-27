package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check

class RulesManager(val rules: List<RuleDetails>) {

    fun checks(policy: RulesPolicy): List<CheckDetails> {
        return rules(policy)
                .flatMap { details ->
                    details.instance::class.java.methods.mapNotNull { method ->
                        method.getAnnotation(Check::class.java)?.let {
                            details.toCheckDetails(it, method)
                        }
                    }
                }
    }

    fun rules(policy: RulesPolicy): List<RuleDetails> {
        return rules.filter { details -> policy.accepts(details.rule) }
    }

    fun size(): Int = rules.size
}
