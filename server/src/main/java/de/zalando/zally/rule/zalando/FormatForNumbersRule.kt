package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger
import io.swagger.models.parameters.AbstractSerializableParameter
import io.swagger.models.parameters.Parameter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FormatForNumbersRule(@Autowired ruleSet: ZalandoRuleSet, @Autowired rulesConfig: Config) : AbstractRule(ruleSet) {
    override val title = "Define Format for Type Number and Integer"
    override val url = "/#171"
    override val violationType = ViolationType.MUST
    override val code = "M018"
    override val guidelinesCode = "171"
    private val description = """Numeric properties must have valid format specified: """

    private val type2format = rulesConfig.getConfig("$name.formats").entrySet()
            .map { (key, config) -> key to config.unwrapped() as List<String> }.toMap()

    @Check
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
            Violation(this, title, description + properties, violationType, url, paths)
        } else null
    }

    private fun Parameter.hasValidFormat(): Boolean =
            this !is AbstractSerializableParameter<*> || isValid(getType(), getFormat())

    private fun isValid(type: String?, format: String?): Boolean = type2format[type]?.let { format in it } ?: true
}