package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.util.PatternUtil.isPathVariable
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class EverySecondPathLevelParameterRule(@Autowired ruleSet: ZalandoRuleSet) : SwaggerRule(ruleSet) {
    override val title = "Every Second Path Level To Be Parameter"
    override val url = "/#143"
    override val violationType = ViolationType.MUST
    override val code = "M005"
    override val guidelinesCode = "143"
    private val DESCRIPTION = "Every second path level must be a path parameter"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filterNot {
            val pathSegments = it.split("/").filter { it.isNotEmpty() }
            pathSegments.filterIndexed { i, segment -> isPathVariable(segment) == (i % 2 == 0) }.isEmpty()
        }
        return if (paths.isNotEmpty()) Violation(this, title, DESCRIPTION, violationType, url, paths) else null
    }
}
