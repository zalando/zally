package de.zalando.zally.rule

import de.zalando.zally.util.ast.ReverseAst
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules. It will sort the output by path.
 */
@Component
class SwaggerRulesValidator(@Autowired rules: RulesManager) : RulesValidator<Swagger>(rules) {
    private var ast: ReverseAst<Swagger>? = null

    override fun parse(content: String): Swagger? {
        return try {
            val swagger = SwaggerParser().parse(content)!!
            ast = ReverseAst.fromObject(swagger).withExtensionMethodNames("getVendorExtensions").build()
            swagger
        } catch (e: Exception) {
            null
        }
    }

    override fun ignore(root: Swagger, pointer: String, ruleId: String) = ast?.isIgnored(pointer, ruleId) ?: false
}
