package org.zalando.zally.core

import org.zalando.zally.rule.api.Rule

data class RulesPolicy(val ignoreRules: List<String>) {
    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.id)
    }

    fun withMoreIgnores(moreIgnores: List<String>) = RulesPolicy(ignoreRules + moreIgnores)
}
