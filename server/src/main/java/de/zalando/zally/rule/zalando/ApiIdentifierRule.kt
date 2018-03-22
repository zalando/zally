package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "215",
    severity = Severity.SHOULD,
    title = "Provide API Identifier"
)
class ApiIdentifierRule {
    private val apiIdPattern = """^[a-z0-9][a-z0-9-:.]{6,62}[a-z0-9]$""".toRegex()

    private val noApiIdDesc = "API Identifier should be provided"
    private val invalidApiIdDesc = "API Identifier doesn't match the pattern $apiIdPattern"

    private val extensionName = "x-api-id"
    private val path = "/info/$extensionName"

    @Check(severity = Severity.SHOULD)
    fun validate(adapter: ApiAdapter): Violation? =
        adapter.withVersion2 { swagger ->
            val apiId = swagger.info?.vendorExtensions?.get(extensionName)
            when {
                apiId == null || apiId !is String -> Violation(noApiIdDesc, listOf(path))
                !apiId.matches(apiIdPattern) -> Violation(invalidApiIdDesc, listOf(path))
                else -> null
            }
        }
}
