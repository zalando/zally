package org.zalando.zally.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.zalando.zally.core.RulesPolicy

@Configuration
class RulesPolicyConfiguration {

    @Value("\${zally.ignoreRules:}")
    private lateinit var ignoredRules: List<String>

    @Bean
    fun rulesPolicy(): RulesPolicy {
        return RulesPolicy(ignoredRules)
    }
}
