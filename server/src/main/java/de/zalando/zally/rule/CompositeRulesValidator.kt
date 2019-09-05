package de.zalando.zally.rule

import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
    private val contextRulesValidator: ContextRulesValidator,
    private val swaggerRulesValidator: SwaggerRulesValidator,
    private val jsonRulesValidator: JsonRulesValidator
) : ApiValidator {

    override fun validate(content: String, policy: RulesPolicy, authorization: String?): List<Result> =
        contextRulesValidator.validate(content, policy, authorization) +
            swaggerRulesValidator.validate(content, policy, authorization) +
            jsonRulesValidator.validate(content, policy, authorization)
}
