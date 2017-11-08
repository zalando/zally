package de.zalando.zally.rule.api

import de.zalando.zally.dto.ViolationType

/**
 * An abstract entity representing an external guideline
 * which can be automatically checked.
 */
interface Rule {

    /** The RuleSet this rule belongs to */
    val ruleSet: RuleSet

    /** An internal identifier for this rule */
    val code: String

    /** An external identifier for the guideline */
    val guidelinesCode: String

    /** A name for this rule */
    val name: String

    /** A title for this rule */
    val title: String

    /** The severity of violations which break this rule */
    val violationType: ViolationType

    /** A location where this rule is documented */
    val url: String?

}
