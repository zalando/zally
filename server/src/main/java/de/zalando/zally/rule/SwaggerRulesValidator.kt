package de.zalando.zally.rule

import de.zalando.zally.rule.api.Rule
import de.zalando.zally.rule.zalando.InvalidApiSchemaRule
import io.swagger.models.Swagger
import io.swagger.parser.SwaggerParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules. It will sort the output by path.
 */
@Component
class SwaggerRulesValidator(@Autowired rules: List<Rule>,
                            @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<Rule, Swagger>(rules, invalidApiRule) {

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
}
