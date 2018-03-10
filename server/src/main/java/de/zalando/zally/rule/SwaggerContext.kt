package de.zalando.zally.rule

import io.swagger.models.Swagger

/**
 * Context for invoking checks against Swagger model.
 */
class SwaggerContext(root: Swagger, policy: RulesPolicy, details: CheckDetails) :
        Context<Swagger>(root, policy, details) {

    override fun accepts(): Boolean = accepts(root)

    /**
     * Confirms whether the current rule should be applied to the specified
     * model part, given the current rules policy and any x-zally-ignores
     * entries.
     * @param root the model part to check
     * @return true iff the rule should be applied.
     */
    fun accepts(root: Swagger): Boolean = accepts(root.vendorExtensions)

    /**
     * Confirms whether the current rule should be applied to the current
     * model part, given the current rules policy and any x-zally-ignores
     * entries.
     * @param vendorExtensions the vendor extensions to check for x-zally-ignores
     * @return true iff the rule should be applied.
     */
    private fun accepts(vendorExtensions: MutableMap<String, Any>?): Boolean {
        val moreIgnores = vendorExtensions?.get(zallyIgnoreExtension)
        val localPolicy = if (moreIgnores is Iterable<*>) {
            policy.withMoreIgnores(moreIgnores.filterNotNull().map(Any::toString))
        } else {
            policy
        }
        return localPolicy.accepts(details.rule)
    }
}
