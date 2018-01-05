package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger
import io.swagger.models.parameters.AbstractSerializableParameter
import io.swagger.models.parameters.Parameter
import org.springframework.beans.factory.annotation.Autowired

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "171",
        severity = Severity.MUST,
        title = "Define Format for Type Number and Integer"
)
class FormatForNumbersRule(@Autowired rulesConfig: Config) : AbstractRule() {
    private val description = """Numeric properties must have valid format specified: """

    private val type2format = rulesConfig.getConfig("$name.formats").entrySet()
            .map { (key, config) -> key to config.unwrapped() as List<String> }.toMap()

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val fromObjects = swagger.getAllJsonObjects().flatMap { (def, path) ->
            val badProps = def.entries.filterNot { (_, prop) -> isValid(prop.type, prop.format) }.map { it.key }
            if (badProps.isNotEmpty()) listOf(badProps to path) else emptyList()
        }
        val fromParams = swagger.parameters.orEmpty().entries.flatMap { (name, param) ->
            if (!param.hasValidFormat()) listOf(listOf(name) to "#/parameters/$name") else emptyList()
        }
        val fromPathParams = swagger.paths.orEmpty().entries.flatMap { (name, path) ->
            path.operations.orEmpty().flatMap { operation ->
                val badParams = operation.parameters.orEmpty().filterNot { it.hasValidFormat() }.map { it.name }
                if (badParams.isNotEmpty()) listOf(badParams to name) else emptyList()
            }
        }
        val result = fromObjects + fromParams + fromPathParams
        return if (result.isNotEmpty()) {
            val (props, paths) = result.unzip()
            val properties = props.flatten().toSet().joinToString(", ")
            Violation(description + properties, paths)
        } else null
    }

    private fun Parameter.hasValidFormat(): Boolean =
            this !is AbstractSerializableParameter<*> || isValid(getType(), getFormat())

    private fun isValid(type: String?, format: String?): Boolean = type2format[type]?.let { format in it } ?: true
}