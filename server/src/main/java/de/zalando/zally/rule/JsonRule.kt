package de.zalando.zally.rule

import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.rule.api.RuleSet
import org.springframework.beans.factory.annotation.Autowired

abstract class JsonRule(@Autowired ruleSet: RuleSet) : AbstractRule(ruleSet) {

    abstract fun validate(swagger: JsonNode): Iterable<Violation>

}
