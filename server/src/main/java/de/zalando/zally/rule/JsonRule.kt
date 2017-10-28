package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.beans.factory.annotation.Autowired

abstract class JsonRule(@Autowired ruleSet: RuleSet) : AbstractRule(ruleSet) {

    fun accepts(swagger: JsonNode): Boolean {
        val ignoredCodes = swagger.get(zallyIgnoreExtension)
        return ignoredCodes == null
                || !ignoredCodes.isArray
                || code !in ignoredCodes.map { it.asText() }
    }

    abstract fun validate(swagger: JsonNode): Iterable<Violation>

}
