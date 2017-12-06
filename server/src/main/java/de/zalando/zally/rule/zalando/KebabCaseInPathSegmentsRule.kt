package de.zalando.zally.rule.zalando

import de.zalando.zally.rule.AbstractRule
import de.zalando.zally.rule.api.Check
import de.zalando.zally.rule.api.Severity
import de.zalando.zally.rule.api.Violation
import de.zalando.zally.util.PatternUtil
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class KebabCaseInPathSegmentsRule(@Autowired ruleSet: ZalandoRuleSet) : AbstractRule(ruleSet) {

    override val title = "Lowercase words with hyphens"
    override val id = "129"
    override val severity = Severity.MUST
    private val description = "Use lowercase separate words with hyphens for path segments"

    @Check(severity = Severity.MUST)
    fun validate(swagger: Swagger): Violation? {
        val paths = swagger.paths.orEmpty().keys.filterNot {
            val pathSegments = it.split("/").filter { it.isNotEmpty() }
            pathSegments.filter { !PatternUtil.isPathVariable(it) && !PatternUtil.isLowerCaseAndHyphens(it) }.isEmpty()
        }
        return if (paths.isNotEmpty()) Violation(description, paths) else null
    }
}
