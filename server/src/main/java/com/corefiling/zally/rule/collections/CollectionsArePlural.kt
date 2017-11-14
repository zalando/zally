package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingRuleSet
import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CollectionsArePlural(@Autowired ruleSet: CoreFilingRuleSet) : CoreFilingSwaggerRule(ruleSet) {
    override val title = "Collection Resources Are Plural"
    override val violationType = ViolationType.SHOULD
    override val description = "Collection resources are plural to indicate that they multiple child resources will be available"

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, _ ->

            val word = pattern
                    .split(Regex("\\W"))
                    .last { it.isNotBlank() }

            if (!isPlural(word)) {
                failures.add("$word in $pattern")
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }
}