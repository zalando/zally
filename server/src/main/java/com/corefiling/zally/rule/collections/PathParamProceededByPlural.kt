package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
import org.apache.commons.lang3.StringUtils.isBlank
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PathParamProceededByPlural(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Path Parameters Are Proceeded by Plurals"
    override val violationType = ViolationType.SHOULD
    override val description = "A plural component proceeds any path parameter component in resource paths"

    override fun validate(swagger: Swagger): Violation? = swagger.paths.orEmpty()
            .flatMap { (pattern, _) ->
                pattern
                        .split('/')
                        .mapIndexed { index, component ->
                            if (isPathParam(component)) {
                                val previous = pattern.split('/')[index - 1]
                                if (isBlank(previous)) {
                                    "paths $pattern: $component parameter has no proceeding component"
                                } else if (isPathParam(previous)) {
                                    "paths $pattern: $component parameter has proceeding parameter $previous rather than a non-parameter"
                                } else if (!isPlural(previous)) {
                                    "paths $pattern: $component parameter has proceeding component '$previous' which appears to be singular"
                                } else {
                                    null
                                }
                            } else {
                                null
                            }
                        }
                        .filterNotNull()
            }
            .takeIf(List<String>::isNotEmpty)
            ?.let { it: List<String> ->
                Violation(this, title, description, violationType, url, it)
            }

    private fun isPathParam(component: String): Boolean {
        return pathParamRegex.matches(component)
    }
}

val pathParamRegex = Regex("\\{[^{}]+}")
