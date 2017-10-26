package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
import org.apache.commons.lang3.StringUtils.isBlank
import org.springframework.stereotype.Component

@Component
class PathParamProceededByPlural : CoreFilingSwaggerRule() {
    override val title = "Path Parameters Are Proceeded by Plurals"
    override val violationType = ViolationType.SHOULD
    override val description = "A plural component proceeds any path parameter component in resource paths"

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        swagger.paths?.forEach { pattern, _ ->

            val components = pattern.split('/')
            var failure = false

            for ((index, component) in components.withIndex()) {
                if (isPathParam(component)) {
                    if (isBlank(components[index-1])) {
                        failure = true
                        break
                    } else if (isPathParam(components[index-1])) {
                        failure = true
                        break
                    } else if (!isPlural(components[index-1])) {
                        failure = true
                        break
                    }
                }
            }

            if (failure) {
                failures.add(pattern)
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }

    private fun isPathParam(component : String) : Boolean {
        return pathParamRegex.matches(component)
    }
}

val pathParamRegex = Regex("\\{[^{}]+}")
