package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import de.zalando.zally.util.ast.ReverseAst
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules. It will sort the output by path.
 */
@Component
class SwaggerRulesValidator(rules: RulesManager) : RulesValidator<Swagger>(rules) {
    private var ast: ReverseAst? = null

    override fun parse(content: String): ContentParseResult<Swagger> {
        return try {
            val swagger = SwaggerParser().parse(content)
            if (swagger === null) {
                ContentParseResult.NotApplicable()
            } else {
                ast = ReverseAst.fromObject(swagger).withExtensionMethodNames("getVendorExtensions").build()
                ContentParseResult.ParsedSuccessfully(swagger)
            }
        } catch (e: Exception) {
            ContentParseResult.NotApplicable()
        }
    }

    override fun ignore(root: Swagger, pointer: JsonPointer, ruleId: String) = ast?.isIgnored(pointer, ruleId) ?: false
}
