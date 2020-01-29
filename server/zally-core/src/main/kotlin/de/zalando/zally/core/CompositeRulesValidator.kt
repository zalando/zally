package de.zalando.zally.core

class CompositeRulesValidator(
    private val contextRulesValidator: ContextRulesValidator,
    private val jsonRulesValidator: JsonRulesValidator
) : ApiValidator {

    override fun validate(content: String, policy: RulesPolicy, authorization: String?): List<Result> =
        contextRulesValidator.validate(content, policy, authorization) +
            jsonRulesValidator.validate(content, policy, authorization)
}
