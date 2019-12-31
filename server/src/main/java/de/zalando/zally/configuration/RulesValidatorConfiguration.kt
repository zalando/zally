package de.zalando.zally.configuration

import de.zalando.zally.core.DefaultContextFactory
import de.zalando.zally.core.ApiValidator
import de.zalando.zally.core.CompositeRulesValidator
import de.zalando.zally.core.ContextRulesValidator
import de.zalando.zally.core.JsonRulesValidator
import de.zalando.zally.core.RulesManager
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
