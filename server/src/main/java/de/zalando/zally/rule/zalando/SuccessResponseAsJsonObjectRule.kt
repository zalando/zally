package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import io.swagger.models.ComposedModel
import io.swagger.models.ModelImpl
import io.swagger.models.Swagger
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "110",
        severity = Severity.MUST,
        title = "Response As JSON Object"
)
class SuccessResponseAsJsonObjectRule : AbstractRule() {
    private val description = "Always Return JSON Objects As Top-Level Data Structures To Support Extensibility"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val paths = ArrayList<String>()
        for ((key, value) in swagger.paths.orEmpty()) {
            for ((method, operation) in value.operationMap) {
                for ((code, response) in operation.responses) {
                    val httpCodeInt = code.toIntOrZero()
                    if (httpCodeInt in 200..299) {
                        val schema = response.schema
                        if (schema != null && "object" != schema.type && !schema.isRefToObject(swagger)) {
                            paths.add("$key $method $code")
                        }
                    }
                }
            }
        }

        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }

    private fun Property?.isRefToObject(swagger: Swagger) =
        if (this is RefProperty && swagger.definitions != null) {
            val model = swagger.definitions[simpleRef]
            (model is ModelImpl && model.type == "object") || model is ComposedModel
        } else false

    private fun String.toIntOrZero() =
        try {
            this.toInt()
        } catch (e: NumberFormatException) {
            0
        }
}
