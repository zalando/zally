package de.zalando.zally.rule

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
        @Autowired val openApiRulesValidator: OpenApiRulesValidator,
        @Autowired val jsonRulesValidator: JsonRulesValidator
) : ApiValidator {

    override fun validate(content: String, requestPolicy: RulesPolicy): List<Result> =
            openApiRulesValidator.validate(content, requestPolicy) +
                    jsonRulesValidator.validate(content, requestPolicy)
}
