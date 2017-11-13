package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import io.swagger.parser.SwaggerParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * This validator validates a given Swagger definition based
 * on set of rules. It will sort the output by path.
 */
@Component
class SwaggerRulesValidator(@Autowired rules: List<SwaggerRule>,
                            @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<SwaggerRule>(rules, invalidApiRule) {

    @Throws(java.lang.Exception::class)
    override fun validator(content: JsonNode): (SwaggerRule) -> Iterable<Violation> {
        val swagger = SwaggerParser().read(content)!!
        return {
            listOfNotNull(it.validate(swagger))
        }
    }

}
