package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NoVersionInUriRule(@Autowired ruleSet: ZalandoRuleSet) : SwaggerRule(ruleSet) {
    override val title = "Do Not Use URI Versioning"
    override val url = "/#115"
    override val violationType = ViolationType.MUST
    override val code = "M009"
    override val guidelinesCode = "115"
    private val description = "basePath attribute contains version number"

    override fun validate(swagger: Swagger): Violation? {
        val hasVersion = swagger.basePath != null && PatternUtil.hasVersionInUrl(swagger.basePath)
        return if (hasVersion) Violation(this, title, description, violationType, url, emptyList()) else null
    }
}
