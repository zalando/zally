package de.zalando.zally.rule

/**
 * Implements x-zally-ignore checking functionality for use at arbitrary
 * vendorExtensions throughout the API specification.
 *
 * @param ruleId the id of the currently running rule
 */
class SwaggerIgnoreExtension(private val ruleId: String) {

    /**
     * Asserts that vendor extension allows this rule to run.
     * @param vendorExtensions the x- prefixed extensions being checked.
     * @return true if x-zally-ignore extension is missing or does not specify the current rule.
     */
    fun accepts(vendorExtensions: Map<String, Any>?): Boolean {
        val ignores = vendorExtensions?.get("x-zally-ignore")
        return if (ignores is Iterable<*>) !ignores.contains(ruleId) else true
    }
}
