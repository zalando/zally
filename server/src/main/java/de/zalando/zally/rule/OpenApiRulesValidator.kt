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

    companion object {

        fun fromContent(content: String): ApiAdapter? {
            return OpenAPIV3Parser().readContents(content, null, ParseOptions())?.let {
                it.openAPI?.let { api -> ApiAdapter(null, api) } ?: swaggerFromContent(content)
            }
        }

        fun fromLocation(location: String): ApiAdapter? {
            val parseResult = OpenAPIV3Parser().readLocation(location, null, ParseOptions())
            return parseResult?.let {
                it.openAPI?.let { api -> ApiAdapter(null, api) } ?: swaggerFromLocation(location)
            }
        }

        private fun swaggerFromContent(content: String): ApiAdapter? {
            return SwaggerConverter().readContents(content, null, ParseOptions()).openAPI?.let { openapi ->
                SwaggerParser().parse(content)?.let { swagger ->
                    ApiAdapter(swagger, openapi)
                }
            }
        }

        private fun swaggerFromLocation(location: String): ApiAdapter? {
            return SwaggerConverter().readLocation(location, null, ParseOptions()).openAPI?.let { openapi ->
                SwaggerParser().read(location)?.let { swagger ->
                    ApiAdapter(swagger, openapi)
                }
            }
        }

    }

    override fun parse(content: String): ApiAdapter? {
        return OpenApiRulesValidator.fromContent(content)
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
