package de.zalando.zally.rule.zalando

import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.SwaggerRule
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Swagger
import io.swagger.models.auth.OAuth2Definition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UsePasswordFlowWithOauth2Rule(@Autowired ruleSet: ZalandoRuleSet) : SwaggerRule(ruleSet) {
    override val title = "Set Flow to Password When Using OAuth 2.0"
    override val url = "/#104"
    override val violationType = ViolationType.SHOULD
    override val code = "M017"
    override val guidelinesCode = "104"

    @Check
    fun validate(swagger: Swagger): Violation? {
        val definitionsWithoutPasswordFlow = swagger
                .securityDefinitions
                .orEmpty()
                .values
                .filter { it.type?.toLowerCase() == "oauth2" }
                .filter { (it as OAuth2Definition).flow != "password" }

        return if (definitionsWithoutPasswordFlow.any())
            Violation(this, title, "OAuth2 security definitions should use password flow", violationType, url, emptyList())
        else null
    }
}
