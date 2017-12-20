package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Swagger
import io.swagger.models.parameters.BodyParameter
import io.swagger.models.parameters.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class AtMostOneBodyParameter(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "At Most One Body Parameter"
    override val violationType = ViolationType.MUST
    override val description = "No more than one body parameter can be associated with an operation"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().map { (method, op) ->
                            val bodies = op.parameters.orEmpty().filter { it is BodyParameter }
                            validate("$pattern $method", bodies)
                        }
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }

    fun validate(location: String, parameters: List<Parameter>): String? {
        return when {
            parameters.size > 1 -> "$location has multiple body parameters ${parameters.map(Parameter::getName)}"
            else -> null
        }
    }
}