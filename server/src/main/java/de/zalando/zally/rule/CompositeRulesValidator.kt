package de.zalando.zally.rule

import de.zalando.zally.dto.LocationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CompositeRulesValidator(
        @Autowired val swaggerRulesValidator: SwaggerRulesValidator,
        @Autowired val jsonRulesValidator: JsonRulesValidator) : ApiValidator {

    override fun validate(swaggerContent: String, ignoreRules: List<String>, locationResolver: LocationResolver): List<Violation> =
            swaggerRulesValidator.validate(swaggerContent, ignoreRules, locationResolver) +
                    jsonRulesValidator.validate(swaggerContent, ignoreRules, locationResolver)

}
