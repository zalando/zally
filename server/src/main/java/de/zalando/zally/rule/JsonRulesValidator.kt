package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class JsonRulesValidator(@Autowired rules: List<JsonRule>,
                         @Autowired invalidApiRule: InvalidApiSchemaRule) : RulesValidator<JsonRule>(rules, invalidApiRule) {

    @Throws(java.lang.Exception::class)
    override fun validator(json: JsonNode): (JsonRule) -> Iterable<Violation> {
        return {
            it.validate(json)
        }
    }
}
