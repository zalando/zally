package org.zalando.zally.core

import com.fasterxml.jackson.core.JsonPointer
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Violation

/**
 * This validator validates a given OpenAPI definition based
 * on set of rules.
 */
class ContextRulesValidator(
    rules: RulesManager,
    private val defaultContextFactory: DefaultContextFactory
) : RulesValidator<Context>(rules) {

    override fun parse(content: String, authorization: String?): ContentParseResult<Context> {
        // first try to parse an OpenAPI (version 3+)
        val parsedAsOpenApi = defaultContextFactory.parseOpenApiContext(content, authorization)
        if (parsedAsOpenApi !is ContentParseResult.NotApplicable) {
            return parsedAsOpenApi
        }
        // if content was no OpenAPI, try to parse a Swagger (version 2)
        val parsedAsSwagger = defaultContextFactory.parseSwaggerContext(content)
        if (parsedAsSwagger !is ContentParseResult.NotApplicable) {
            return parsedAsSwagger
        }
        return ContentParseResult.ParsedWithErrors(listOf(Violation("No valid Open API specification", EMPTY_JSON_POINTER)))
    }

    override fun ignore(root: Context, pointer: JsonPointer, ruleId: String) = root.isIgnored(pointer, ruleId)
}
