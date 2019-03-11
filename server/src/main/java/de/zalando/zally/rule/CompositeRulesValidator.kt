package de.zalando.zally.rule

import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
    private val contextRulesValidator: ContextRulesValidator,
    private val swaggerRulesValidator: SwaggerRulesValidator,
    private val jsonRulesValidator: JsonRulesValidator
) : ApiValidator {

    override fun validate(content: String, policy: RulesPolicy): List<Result> =
        contextRulesValidator.validate(content, policy) +
            swaggerRulesValidator.validate(content, policy) +
            jsonRulesValidator.validate(content, policy)
}
