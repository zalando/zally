package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.WordUtil.isPlural
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger

@Rule(
        ruleSet = ZalandoRuleSet::class,
        id = "120",
        severity = Severity.SHOULD,
        title = "Array names should be pluralized"
)
class PluralizeNamesForArraysRule {

    @Check(severity = Severity.SHOULD)
    fun validate(swagger: Swagger): Violation? {
        val res = swagger.getAllJsonObjects().map { (def, path) ->
            val badProps = def.entries.filter { "array" == it.value.type && !isPlural(it.key) }
            if (badProps.isNotEmpty()) {
                val propsDesc = badProps.map { "'${it.key}'" }.joinToString(",")
                "$path: $propsDesc" to path
            } else null
        }.filterNotNull()

        return if (res.isNotEmpty()) {
            val (desc, paths) = res.unzip()
            Violation(desc.joinToString("\n"), paths)
        } else null
    }
}
