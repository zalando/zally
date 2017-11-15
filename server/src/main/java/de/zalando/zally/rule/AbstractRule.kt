package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.RuleSet

abstract class AbstractRule(ruleSet: RuleSet) : Rule {

    override val ruleSet = ruleSet

    override val name: String = javaClass.simpleName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractRule) return false

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int = name.hashCode()

    override fun toString(): String = "$code-$name"
}
