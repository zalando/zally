package com.corefiling.zally.rule

import de.zalando.zally.rule.AbstractRule
import org.springframework.beans.factory.annotation.Autowired

abstract class CoreFilingSwaggerRule(@Autowired ruleSet: CoreFilingRuleSet) : AbstractRule(ruleSet) {
    override val id = javaClass.simpleName
    abstract val description: String
}