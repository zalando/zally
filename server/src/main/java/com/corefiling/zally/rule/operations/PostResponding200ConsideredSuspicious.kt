package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import com.corefiling.zally.rule.collections.detectCollection
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.HttpMethod
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PostResponding200ConsideredSuspicious(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "POST Responding 200 Considered Suspicious"
    override val violationType = ViolationType.SHOULD
    override val description = "POST operations should be used to create resources or initiate an asynchronous action, 200 response suggests a synchronous return"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty().flatMap { (pattern, path) ->
                path.operationMap.orEmpty().filterKeys { it==HttpMethod.POST }.flatMap { (method, op) ->
                    op.responses.orEmpty().filterKeys { it=="200" }.map { (status, response) ->
                        validate("$pattern $method response 200 OK", detectCollection(swagger, pattern, path))
                    }
                }
            }.filterNotNull().takeIf { it.isNotEmpty() }?.let { Violation(this, title, description, violationType, it) }

    fun validate(location: String, collection: Boolean): String? {
        return when(collection) {
            true -> "$location probably should be a 201 Created"
            else -> "$location probably should be a 202 Accepted"
        }
    }
}