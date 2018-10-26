package de.zalando.zally.rule

import de.zalando.zally.rule.api.RuleSet

abstract class AbstractRuleSet : RuleSet {

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other != null &&
            this.javaClass == other.javaClass &&
            this.id == (other as RuleSet).id
    }

    override fun toString(): String {
        return id
    }
}
