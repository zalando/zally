package de.zalando.zally.rule

import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules. It will sort the output by path.
 */
@Component
class SwaggerRulesValidator(@Autowired rules: List<SwaggerRule>,
                            @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<SwaggerRule, Swagger>(rules, invalidApiRule) {

    override fun parse(content: String): Swagger? {
        return try {
            SwaggerParser().parse(content)!!
        } catch (e: Exception) {
            null
        }
    }

    override fun ignores(root: Swagger): List<String> {
        val ignores = root.vendorExtensions?.get(zallyIgnoreExtension)
        return if (ignores is Iterable<*>) {
            ignores.map { it.toString() }
        } else {
            emptyList()
        }
    }

    override fun validator(root: Swagger): (SwaggerRule) -> Iterable<Violation> {
        return {
            listOfNotNull(it.validate(root))
        }
    }

}
