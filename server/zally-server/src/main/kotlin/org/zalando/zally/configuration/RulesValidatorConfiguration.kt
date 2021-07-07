package org.zalando.zally.configuration

import org.zalando.zally.core.DefaultContextFactory
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.CompositeRulesValidator
import org.zalando.zally.core.ContextRulesValidator
import org.zalando.zally.core.JsonRulesValidator
import org.zalando.zally.core.RulesManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class RulesValidatorConfiguration {

    @Autowired
    private lateinit var rules: RulesManager

    @Autowired
    private lateinit var defaultContextFactory: DefaultContextFactory

    @Bean
    @Primary
    fun validator(): ApiValidator {
        return CompositeRulesValidator(
            ContextRulesValidator(rules, defaultContextFactory),
            JsonRulesValidator(rules)
        )
    }
}
