package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.Context
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "174",
        severity = Severity.MUST,
        title = "Use common field names"
)
class CommonFieldTypesRule(@Autowired rulesConfig: Config) {

    @Suppress("UNCHECKED_CAST")
    private val commonFields = rulesConfig.getConfig("${javaClass.simpleName}.common_types").entrySet()
            .map { (key, config) -> key to config.unwrapped() as List<String?> }.toMap()

    fun checkField(name: String, property: Schema<Any>): String? =
            commonFields[name.toLowerCase()]?.let { (type, format) ->
                if (property.type != type)
                    "field '$name' has type '${property.type}' (expected type '$type')"
                else if (property.format != format && format != null)
                    "field '$name' has type '${property.type}' with format '${property.format}' (expected format '$format')"
                else null
            }

    private fun allSchemas(api: OpenAPI): List<Map.Entry<String, Schema<Any>>> {
        val objectSchemas = (api.components.schemas.orEmpty().values +
                api.components.responses.values.flatMap { it.content.values.map { it.schema } } +
                api.components.requestBodies.values.flatMap { it.content.values.map { it.schema } } +
                api.paths.orEmpty().flatMap {
                    it.value.readOperations().flatMap {
                        it.parameters.orEmpty()
                                .map { it.schema }
                    }
                } +
                api.paths.orEmpty().flatMap {
                    it.value.readOperations().flatMap {
                        it.responses.orEmpty()
                                .flatMap { it.value.content.orEmpty().values.map { it.schema } }
                    }
                })
        return objectSchemas.flatMap { it.properties.orEmpty().entries }
    }

    @Check(severity = Severity.MUST)
    fun checkTypesOfCommonFields(context: Context): List<Violation> {
        return allSchemas(context.api)
                .map { (name, schema) -> Pair(checkField(name, schema).orEmpty(), schema) }
                .filterNot { it.first.isEmpty() }
                .map { context.violation(it.first, it.second) }
    }
}
