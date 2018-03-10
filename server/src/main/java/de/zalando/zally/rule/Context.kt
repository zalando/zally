package de.zalando.zally.rule

/**
 * Context for invoking checks, providing references to the model being
 * checked, the rules policy, the Check, Rule and RuleSet metadata.
 */
abstract class Context<out RootT>(val root: RootT, val policy: RulesPolicy, val details: CheckDetails) {
    protected val zallyIgnoreExtension = "x-zally-ignore"

    /**
     * Confirms whether the current rule should be applied to the current
     * model, given the current rules policy and any x-zally-ignores entries.
     * @return true iff the rule should be applied.
     */
    abstract fun accepts(): Boolean
}