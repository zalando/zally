package de.zalando.zally.rule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
    @Autowired private val contextRulesValidator: ContextRulesValidator,
    @Autowired private val swaggerRulesValidator: SwaggerRulesValidator,
    @Autowired private val jsonRulesValidator: JsonRulesValidator
) : ApiValidator {

    override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> =
        contextRulesValidator.validate(content, requestPolicy) +
            swaggerRulesValidator.validate(content, requestPolicy) +
            jsonRulesValidator.validate(content, requestPolicy)
}
