package de.zalando.zally.rule

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules.
 */
@Component
class SwaggerRulesValidator(@Autowired rules: RulesManager) : RulesValidator<Swagger>(rules) {

    override fun parse(content: String): Swagger? {
        return try {
            SwaggerParser().parse(content)
        } catch (e: Exception) {
            null
        }
    }

    override fun context(root: Swagger, policy: RulesPolicy, details: CheckDetails): Context<Swagger> =
            SwaggerContext(root, policy, details)
}
