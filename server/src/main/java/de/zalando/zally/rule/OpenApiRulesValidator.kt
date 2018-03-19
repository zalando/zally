package de.zalando.zally.rule

import io.swagger.parser.SwaggerParser
import io.swagger.v3.parser.OpenAPIV3Parser
import io.swagger.v3.parser.converter.SwaggerConverter
import io.swagger.v3.parser.core.models.ParseOptions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given OpenAPI 3 definition based
 * on set of rules. It also supports Swagger V2 as a fallback.
 * It will sort the output by path.
 */
@Component
class OpenApiRulesValidator(@Autowired rules: RulesManager) : RulesValidator<ApiAdapter>(rules) {

    override fun parse(content: String): ApiAdapter? {
        return OpenAPIV3Parser().read(content)?.let { ApiAdapter(null, it) } ?: convertSwaggerV2(content)
    }

    private fun convertSwaggerV2(content: String): ApiAdapter? {
        return SwaggerConverter().readContents(content, null, ParseOptions()).openAPI?.let { openapi ->
            SwaggerParser().parse(content)?.let { swagger ->
                ApiAdapter(swagger, openapi)
            }
        }
    }

    override fun ignores(root: ApiAdapter): List<String> {
        val ignores = root.vendorExtensions?.get(zallyIgnoreExtension)
        return if (ignores is Iterable<*>) {
            ignores.map { it.toString() }
        } else {
            emptyList()
        }
    }
}
