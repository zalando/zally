package org.zalando.zally.ruleset.zalando

import com.typesafe.config.Config
import io.swagger.v3.oas.models.media.Schema
import org.zalando.zally.core.plus
import org.zalando.zally.core.toJsonPointer
import org.zalando.zally.core.util.getAllSchemas
import org.zalando.zally.core.util.isObjectSchema
import org.zalando.zally.rule.api.Check
import org.zalando.zally.rule.api.Context
import org.zalando.zally.rule.api.Rule
import org.zalando.zally.rule.api.Severity
import org.zalando.zally.rule.api.Violation

@Rule(
    ruleSet = ZalandoRuleSet::class,
    id = "174",
    severity = Severity.MUST,
    title = "Use common field names"
)
class CommonFieldTypesRule(rulesConfig: Config) {

    @Suppress("UNCHECKED_CAST")
    private val commonFields = rulesConfig.getConfig("${javaClass.simpleName}.common_types").entrySet()
        .associate { (key, config) -> key to config.unwrapped() as List<String?> }

    @Check(severity = Severity.MUST)
    fun checkTypesOfCommonFields(context: Context): List<Violation> =
        context.api.getAllSchemas().flatMap {
            checkAllPropertiesOf(it, check = { parentObjectSchema, name, schema ->
                val violationDesc = checkField(name, schema)
                if (violationDesc != null) {
                    if (schema.isObjectSchema()) {
                        context.violations(violationDesc, schema)
                    } else {
                        context.violations(
                            violationDesc,
                            context.getJsonPointer(parentObjectSchema) + "/properties/$name".toJsonPointer()
                        )
                    }
                } else {
                    emptyList()
                }
            })
        }

    private fun checkAllPropertiesOf(
        objectSchema: Schema<Any>,
        check: (parentObject: Schema<Any>, name: String, schema: Schema<Any>) -> Collection<Violation>
    ): Collection<Violation> {

        fun traverse(oSchema: Schema<Any>): List<Violation?> = oSchema.properties.orEmpty().flatMap { (name, schema) ->
            if (schema.properties.isNullOrEmpty()) {
                check(oSchema, name, schema)
            } else {
                traverse(schema)
            }
        }

        return traverse(objectSchema).filterNotNull()
    }

    internal fun checkField(name: String, property: Schema<Any>): String? =
        commonFields[name]?.let { (type, format) ->
            if (property.type != type)
                "field '$name' has type '${property.type}' (expected type '$type')"
            else if (property.format != format && format != null)
                "field '$name' has type '${property.type}' with format '${property.format}' (expected format '$format')"
            else null
        }
}
