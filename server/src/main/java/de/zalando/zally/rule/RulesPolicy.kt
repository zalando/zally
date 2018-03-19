package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Represents a policy deciding which rules should be used and which should be ignored.
 * Implementation takes a blacklist of rules to be ignored and assumes others are to be applied.
 */
@Component
class RulesPolicy(@Value("\${zally.ignoreRules:}") private val ignoreRules: Array<String>) {

    fun accepts(rule: Rule): Boolean {
        return !ignoreRules.contains(rule.id)
    }

    /**
     * Creates a RulesPolicy based on this one + a specified list of more ignores
     * @param moreIgnores the new ignores to add
     * @return this iff moreIgnores is empty, otherwise a new new RulesPolicy
     */
    fun withMoreIgnores(moreIgnores: List<String>) =
            if (moreIgnores.isEmpty()) this
            else RulesPolicy(ignoreRules + moreIgnores)
}
