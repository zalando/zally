package de.zalando.zally.rule

import de.zalando.zally.rule.api.Check
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
        return { rule: SwaggerRule ->

            val violations = mutableListOf<Violation>()

            rule::class.java.methods
                    .filter { it.isAnnotationPresent(Check::class.java) }
                    .filter { it.parameters.size == 1 }
                    .filter { it.parameters[0].type.isAssignableFrom(root::class.java) }
                    .forEach {
                        it.isAccessible = true
                        if (it.returnType == Violation::class.java) {
                            val violation = it.invoke(rule, root) as Violation?
                            if (violation != null) {
                                violations.add(violation)
                            }
                        }
                    }

            violations
        }
    }

}
