package de.zalando.zally.core

import de.zalando.zally.rule.api.Rule

data class RulesPolicy(val ignoreRules: List<String>) {
    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.id)
    }

    fun withMoreIgnores(moreIgnores: List<String>) = RulesPolicy(ignoreRules + moreIgnores)
}
