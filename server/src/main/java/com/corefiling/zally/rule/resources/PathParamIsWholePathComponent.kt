package com.corefiling.zally.rule.resources

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import com.corefiling.zally.rule.collections.ifNotEmptyLet
import com.corefiling.zally.rule.collections.pathParamRegex
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.rule.api.Check
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PathParamIsWholePathComponent(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Path Parameters Are Entire Path Components"
    override val violationType = ViolationType.MUST
    override val description = "Path parameters occupy an entire path component between slashes, never a partial component"

    @Check
    fun validate(swagger: Swagger): Violation? =
            swagger.paths.orEmpty()
                    .map { (pattern, _) ->
                        pattern
                                .split('/')
                                .filter { pathParamRegex.find(it) != null }
                                .filter { pathParamRegex.replaceFirst(it, "XXXXX") != "XXXXX" }
                                .ifNotEmptyLet { "$pattern contains partial component path parameters: ${it.joinToString()}" }
                    }
                    .ifNotEmptyLet { Violation(this, title, description, violationType, it) }
}