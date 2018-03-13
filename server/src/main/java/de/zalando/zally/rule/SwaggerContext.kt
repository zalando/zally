package de.zalando.zally.rule

import de.zalando.zally.rule.api.Violation
import io.swagger.models.Path
import io.swagger.models.Swagger

/**
 * Context for invoking checks against Swagger model.
 */
class SwaggerContext(root: Swagger, policy: RulesPolicy, details: CheckDetails) :
        Context<Swagger>(root, policy, details) {

    /**
     * Validate paths in the model, ignoring according to x-zally-ignores,
     * and apply the supplied function to what remains.
     * @param description the description to use in any resulting Violation
     * @param toMessages the function to validate the model
     * @return a Violation instance iff toMessages returns some messages
     */
    fun validatePaths(
        description: String,
        toMessages: (swagger: Swagger, pattern: String, path: Path) -> List<String>
    ): Violation? =
            validateSwagger(description) { swagger ->
                swagger.paths
                        .filterValues { accepts(it) }
                        .flatMap { (pattern, path) ->
                            toMessages(swagger, pattern, path).map { "paths: $pattern: $it" }
                        }
            }

    /**
     * Validate the root of the model, ignoring according to x-zally-ignores,
     * and apply the supplied function to what remains.
     * @param description the description to use in any resulting Violation
     * @param toMessages the function to validate the model
     * @return a Violation instance iff toMessages returns some messages
     */
    fun validateSwagger(
        description: String,
        toMessages: (swagger: Swagger) -> List<String>
    ): Violation? =
            toMessages(root).takeIf { it.isNotEmpty() }?.let { Violation(description, it) }

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
     * @param path the vendor extensions to check for x-zally-ignores
     * @return true iff the rule should be applied.
     */
    fun accepts(path: Path): Boolean = accepts(path.vendorExtensions)

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
