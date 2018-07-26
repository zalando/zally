package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Context
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given OpenAPI definition based
 * on set of rules.
 */
@Component
class ContextRulesValidator(@Autowired rules: RulesManager) : RulesValidator<Context>(rules) {

    override fun parse(content: String): Context? =
            DefaultContext.createOpenApiContext(content) ?: DefaultContext.createSwaggerContext(content)

    override fun ignore(root: Context, pointer: JsonPointer, ruleId: String) = root.isIgnored(pointer, ruleId)
}
