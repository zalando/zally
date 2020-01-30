package de.zalando.zally.rule.api

import java.net.URI

/**
 * A logical grouping of rules that can be enabled and disabled as a whole.
 * RuleSets can be used to represent a company's guidelines or to define
 * finer grained logical groupings such as technical vs semantic rules.
 */
interface RuleSet {

    /** A unique identifier for the RuleSet */
    val id: String

    /** The base url where documentation for this RuleSet can be found */
    val url: URI

    /** Calculate url where a Rule is documented */
    fun url(rule: Rule): URI
}
