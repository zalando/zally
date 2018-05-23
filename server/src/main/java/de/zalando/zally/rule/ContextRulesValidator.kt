package de.zalando.zally.rule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given OpenAPI definition based
 * on set of rules.
 */
@Component
class ContextRulesValidator(@Autowired rules: RulesManager) : RulesValidator<Context>(rules) {

    override fun parse(content: String): Context? =
            Context.createOpenApiContext(content) ?: Context.createSwaggerContext(content)

    override fun ignore(root: Context, pointer: String, ruleId: String) = root.isIgnored(pointer, ruleId)
}
