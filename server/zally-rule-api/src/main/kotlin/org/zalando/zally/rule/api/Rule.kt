package org.zalando.zally.rule.api

import kotlin.reflect.KClass

/**
 * An abstract entity representing an external guideline
 * which can be automatically checked.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Rule(
    /** The RuleSet this rule belongs to  */
    val ruleSet: KClass<out RuleSet>,
    /** The identifier for this rule  */
    val id: String,
    /** The stated severity for this rule  */
    val severity: Severity,
    /** A title for this rule  */
    val title: String
)
