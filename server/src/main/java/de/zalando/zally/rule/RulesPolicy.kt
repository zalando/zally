package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule

data class RulesPolicy(val ignoreRules: Array<String>) {

    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.id)
    }

    fun withMoreIgnores(moreIgnores: List<String>) = this.copy(ignoreRules = ignoreRules + moreIgnores)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RulesPolicy

        if (!ignoreRules.contentEquals(other.ignoreRules)) return false

        return true
    }

    override fun hashCode(): Int {
        return ignoreRules.contentHashCode()
    }
}
