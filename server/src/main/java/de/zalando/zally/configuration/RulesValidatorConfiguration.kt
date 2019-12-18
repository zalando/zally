package de.zalando.zally.configuration

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.rule.ApiValidator
import de.zalando.zally.rule.CompositeRulesValidator
import de.zalando.zally.rule.ContextRulesValidator
import de.zalando.zally.rule.JsonRulesValidator
import de.zalando.zally.rule.RulesManager
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
