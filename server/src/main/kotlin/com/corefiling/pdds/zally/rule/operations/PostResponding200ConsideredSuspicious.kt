package com.corefiling.pdds.zally.rule.operations

import com.corefiling.pdds.zally.extensions.validateOperation
import com.corefiling.pdds.zally.rule.CoreFilingRuleSet
import com.corefiling.pdds.zally.rule.collections.detectCollection
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
class PostResponding200ConsideredSuspicious {
    val description = "POST operations should be used to create resources or initiate an asynchronous action, 200 response suggests a synchronous return"

    @Check(Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? =
            swagger.validateOperation(description) { pattern, path, method, op ->
                when {
                    method != HttpMethod.POST -> null
                    op.responses["200"] == null -> null
                    detectCollection(swagger, pattern, path) -> "response 200 OK probably should be a 201 Created"
                    else -> "response 200 OK probably should be a 202 Accepted"
                }
            }
}