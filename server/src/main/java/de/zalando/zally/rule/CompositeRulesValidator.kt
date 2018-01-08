package de.zalando.zally.rule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
        @Autowired val swaggerRulesValidator: SwaggerRulesValidator,
        @Autowired val jsonRulesValidator: JsonRulesValidator) : ApiValidator {

    override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> =
            swaggerRulesValidator.validate(content, requestPolicy) +
                    jsonRulesValidator.validate(content, requestPolicy)
}
