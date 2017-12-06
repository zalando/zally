package de.zalando.zally.rule.api

/**
 * An abstract entity representing an external guideline
 * which can be automatically checked.
 */
interface Rule {

    /** The RuleSet this rule belongs to */
    val ruleSet: RuleSet

    /** The identifier for this rule */
    val id: String

    /** A title for this rule */
    val title: String

    /** The stated severity for this rule */
    val severity: Severity
}
