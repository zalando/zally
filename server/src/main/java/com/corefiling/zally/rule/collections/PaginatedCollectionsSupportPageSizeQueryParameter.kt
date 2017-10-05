package com.corefiling.zally.rule.collections

import com.corefiling.zally.rule.CoreFilingSwaggerRule
import de.zalando.zally.dto.ViolationType
import de.zalando.zally.rule.Violation
import io.swagger.models.Swagger
import io.swagger.models.parameters.Parameter
import io.swagger.models.parameters.PathParameter
import io.swagger.models.parameters.QueryParameter
import io.swagger.models.properties.Property
import io.swagger.models.properties.RefProperty
import io.swagger.parser.ResolverCache
import org.springframework.stereotype.Component
import java.math.BigDecimal
import javax.websocket.server.PathParam

@Component
class PaginatedCollectionsSupportPageSizeQueryParameter : CoreFilingSwaggerRule() {
    override val title = "Paginated Resources Support 'pageSize' Query Parameter"
    override val violationType = ViolationType.SHOULD
    private val DESCRIPTION = "Paginated resources support a 'pageSize' query parameter " +
            "with type:integer, format:int32, minimum:1 so that clients can easily iterate over the collection."

    override fun validate(swagger: Swagger): Violation? {

        var failures = mutableListOf<String>()

        collectionPaths(swagger)?.forEach { pattern, path ->
            if (path.get!=null) {

                var found = false;

                path.get.parameters?.forEach { param ->
                    if (param is QueryParameter && param.name=="pageSize") {
                        found = true
                        if(param.type!="integer" ||
                                param.format!="int32" ||
                                param.minimum!=BigDecimal(1)) {
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