package net.oxspring.zally.rule.resources

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class SensibleSlashes : SwaggerRule() {
    override val title = "SensibleSlashes"
    override val url = "#SensibleSlashes"
    override val violationType = ViolationType.MUST
    override val code = "SensibleSlashes"
    override val guidelinesCode = "SensibleSlashes"
    private val DESCRIPTION = "Rule avoid trailing slashes is not followed"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filter { it != null && PatternUtil.hasTrailingSlash(it) }
        return if (!paths.isEmpty()) Violation(this, title, DESCRIPTION, violationType, url, paths) else null
    }
}