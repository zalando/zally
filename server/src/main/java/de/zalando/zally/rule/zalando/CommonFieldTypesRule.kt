package de.zalando.zally.rule.zalando

import com.typesafe.config.Config
import de.zalando.zally.rule.ApiAdapter
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.properties.Property
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

    fun checkField(name: String, property: Property): String? =
        commonFields[name.toLowerCase()]?.let { (type, format) ->
            if (property.type != type)
                "field '$name' has type '${property.type}' (expected type '$type')"
            else if (property.format != format && format != null)
                "field '$name' has type '${property.type}' with format '${property.format}' (expected format '$format')"
            else null
        }

    @Check(severity = Severity.MUST)
    fun validate(adapter: ApiAdapter): Violation? =
        adapter.withVersion2 { swagger ->
            val res = swagger.getAllJsonObjects().map { (def, path) ->
                val badProps = def.entries.map { checkField(it.key, it.value) }.filterNotNull()
                if (badProps.isNotEmpty())
                    (path + ": " + badProps.joinToString(", ")) to path
                else null
            }.filterNotNull()

            if (res.isNotEmpty()) {
                val (desc, paths) = res.unzip()
                Violation(desc.joinToString(", "), paths)
            } else null
        }

}
