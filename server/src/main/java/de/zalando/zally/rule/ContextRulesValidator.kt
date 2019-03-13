package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.rule.api.Context
import org.springframework.stereotype.Component

/**
 * This validator validates a given OpenAPI definition based
 * on set of rules.
 */
@Component
class ContextRulesValidator(rules: RulesManager) : RulesValidator<Context>(rules) {

    override fun parse(content: String): ContentParseResult<Context> {
        // first try to parse an OpenAPI (version 3+)
        val parsedAsOpenApi = DefaultContext.createOpenApiContext(content)
        return when (parsedAsOpenApi) {
            is ContentParseResult.NotApplicable ->
                // if content was no OpenAPI, try to parse a Swagger (version 2)
                DefaultContext.createSwaggerContext(content)
            else ->
                parsedAsOpenApi
        }
    }

    override fun ignore(root: Context, pointer: JsonPointer, ruleId: String) = root.isIgnored(pointer, ruleId)
}
