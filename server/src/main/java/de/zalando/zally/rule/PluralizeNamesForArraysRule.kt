package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.util.WordUtil.isPlural
import de.zalando.zally.util.getAllJsonObjects
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PluralizeNamesForArraysRule(@Autowired ruleSet: ZalandoRuleSet) : SwaggerRule(ruleSet) {
    override val title = "Array names should be pluralized"
    override val url = "/#120"
    override val violationType = ViolationType.SHOULD
    override val code = "S007"
    override val guidelinesCode = "120"

    override fun validate(swagger: Swagger): Violation? {
        val res = swagger.getAllJsonObjects().map { (def, path) ->
            val badProps = def.entries.filter { "array" == it.value.type && !isPlural(it.key) }
            if (badProps.isNotEmpty()) {
                val propsDesc = badProps.map { "'${it.key}'" }.joinToString(",")
                "$path: $propsDesc" to path
            } else null
        }.filterNotNull()

        return if (res.isNotEmpty()) {
            val (desc, paths) = res.unzip()
            Violation(this, title, desc.joinToString("\n"), violationType, url, paths)
        } else null
    }
}
