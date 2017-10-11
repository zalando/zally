package de.zalando.zally.rule

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class AvoidTrailingSlashesRule() : SwaggerRule() {
    override val title = "Avoid Trailing Slashes"
    override val url = "/#136"
    override val violationType = ViolationType.MUST
    override val code = "M002"
    override val guidelinesCode = "136"
    private val DESCRIPTION = "Rule avoid trailing slashes is not followed"

    override fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filter { it != null && PatternUtil.hasTrailingSlash(it) }
        val specPointers = paths.map(SpecPointerProvider::forPath)
        return if (!paths.isEmpty()) Violation(this, title, DESCRIPTION, violationType, url, paths, specPointers) else null
    }
}
