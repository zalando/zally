package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import io.swagger.models.parameters.QueryParameter
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class PaginatedCollectionsSupportPageNumberQueryParameter : CoreFilingSwaggerRule() {
    override val title = "Paginated Resources Support 'pageNumber' Query Parameter"
    override val violationType = ViolationType.SHOULD
    private val DESCRIPTION = "Paginated resources support a 'pageNumber' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    override fun validate(swagger: Swagger): Violation? {

        var failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, path ->
            if (path.get!=null) {

                var found = false

                path.get.parameters?.forEach { param ->
                    if (param is QueryParameter && param.name=="pageNumber") {
                        found = true
                        if (param.type!="integer" ||
                                param.format!="int32" ||
                                param.minimum!= BigDecimal(1) ||
                                param.required!=true) {
                            failures.add(pattern + " GET")
                        }
                    }
                }

                if (!found) {
                    failures.add(pattern + " GET")
                }
            }
        }

        return if (failures.isEmpty()) null else
            Violation(this, title, DESCRIPTION, violationType, url, failures)
    }
}