package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CollectionsReturnXTotalItemsHeader(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Collection Resources Return X-Total-Items Header"
    override val violationType = ViolationType.SHOULD
    override val description = "Collection resources return the X-Total-Items header " +
            "with type:integer and format:int32 so that clients can easily access the total"

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, path ->
            if (path.get!=null) {
                path.get.responses?.forEach { code, response ->
                    if (Integer.parseInt(code) < 300) {
                        val header = response.headers?.get("X-Total-Items")
                        if (header == null || header.type != "integer" || header.format != "int32") {
                            failures.add(pattern + " GET " + code)
                        }
                    }
                }
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }
}