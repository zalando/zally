package de.zalando.zally.configuration

import de.zalando.zally.rule.ApiValidator
import de.zalando.zally.rule.CompositeRulesValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class RulesValidatorConfiguration {

    @Autowired
    private lateinit var compositeValidator: CompositeRulesValidator

    @Bean
    @Primary
    fun validator(): ApiValidator {
        return compositeValidator
    }
}
