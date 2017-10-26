package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import de.zalando.zally.util.WordUtil.isPlural
import io.swagger.models.Swagger
import org.springframework.stereotype.Component

@Component
class CollectionsArePlural : CoreFilingSwaggerRule() {
    override val title = "Collection Resources Return Arrays"
    override val violationType = ViolationType.SHOULD
    override val description = "Collection resources return arrays so that they can be acted upon easily"

    override fun validate(swagger: Swagger): Violation? {

        val failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, _ ->

            val word = pattern.split(Regex("\\W")).last()

            if (!isPlural(word)) {
                failures.add(pattern)
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, description, violationType, url, failures)
    }
}