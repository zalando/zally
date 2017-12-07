package de.zalando.zally.rule.api

import de.zalando.zally.dto.ViolationType

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

    /** The severity of violations which break this rule */
    val violationType: ViolationType
}
