package de.zalando.zally.rule

import de.zalando.zally.rule.api.RuleSet
import io.swagger.models.Swagger

abstract class SwaggerRule(ruleSet: RuleSet) : AbstractRule(ruleSet) {

    abstract fun validate(swagger: Swagger): Violation?

}
