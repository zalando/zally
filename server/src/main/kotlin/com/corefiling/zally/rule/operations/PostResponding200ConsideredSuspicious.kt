package com.corefiling.zally.rule.operations

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.collections.detectCollection
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.models.HttpMethod
import io.swagger.models.Swagger

@Rule(
        ruleSet = CoreFilingRuleSet::class,
        id = "PostResponding200ConsideredSuspicious",
        severity = Severity.SHOULD,
        title = "POST Responding 200 Considered Suspicious"
)
class PostResponding200ConsideredSuspicious : AbstractRule() {
    val description = "POST operations should be used to create resources or initiate an asynchronous action, 200 response suggests a synchronous return"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .flatMap { (pattern, path) ->
                        path.operationMap.orEmpty().filterKeys { it == HttpMethod.POST }.flatMap { (method, op) ->
                            op.responses.orEmpty().filterKeys { it == "200" }.map { (_, _) ->
                                validate("$pattern $method response 200 OK", detectCollection(swagger, pattern, path))
                            }
                        }
                    }
                    .ifNotEmptyLet { Violation(description, it) }

    fun validate(location: String, collection: Boolean): String? {
        return when (collection) {
            true -> "$location probably should be a 201 Created"
            else -> "$location probably should be a 202 Accepted"
        }
    }
}